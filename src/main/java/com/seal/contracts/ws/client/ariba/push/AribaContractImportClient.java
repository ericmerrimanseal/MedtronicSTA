package com.seal.contracts.ws.client.ariba.push;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceImportReply;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.ContractWorkspaceImportRequest;
import com.seal.contracts.ariba.wsdl.contractworkspaceimport.WSContractWorkspaceOutputBean;
import com.seal.contracts.ariba.wsdl.document.*;
import com.seal.contracts.generator.csv.bean.Contract;
import com.seal.contracts.generator.csv.service.ConfigService;
import com.seal.contracts.generator.persistence.entity.Config;
import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportDocumentStatus;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import com.seal.contracts.generator.csv.service.ImportService;
import com.seal.contracts.ws.client.seal.SealService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.seal.contracts.generator.csv.bean.Contract.HIERARCHICAL_TYPE.SubAgreement;
import static com.seal.contracts.generator.persistence.entity.enums.ContractImportDocumentStatus.IMPORT_FAILED;
import static com.seal.contracts.generator.persistence.entity.enums.ContractImportDocumentStatus.READY;
import static com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus.*;
import static com.seal.contracts.generator.csv.service.ImportService.STATUS.IDLE;
import static com.seal.contracts.generator.csv.service.ImportService.STATUS.IN_PROGRESS;

/**
 * Created by jantonak on 09.11.15..
 */
@Slf4j
@Service
public class AribaContractImportClient implements ImportService {

    private static final BigInteger OK = BigInteger.ZERO;
    private static String PASSWORD_ADAPTER = "PasswordAdapter1";

    private enum Action {Create, Update}


    @Autowired
    private WebServiceTemplate template;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ContractImportItemRepository itemsRepository;

    @Autowired
    private ContractImportDocumentRepository documentsRepository;

    @Autowired
    private SealService sealService;

    private final Producer producer;

    public AribaContractImportClient() {
        producer = new Producer(this);
        producer.start();
        log.info("Producer started");
    }

    @Override
    public int getLeft() {
        return producer.getLeft();
    }

    @Override
    public void add(List<ContractImportItem> items) throws ImportException {
        producer.addAsync(items);
//        notify();
    }

    @Override
    public STATUS getStatus() {
        return producer.getStatus();
    }

    @Override
    public List<Consumer> getConsumers() {
        return producer.getConsumers();
    }

    private class Producer extends Thread {

        private LinkedList<ContractImportItem> toProcess = Lists.newLinkedList();

        private LinkedList<ContractImportItem> received = Lists.newLinkedList();

        @Getter
        private List<Consumer> consumers = Lists.newArrayList();

        private final AribaContractImportClient parent;

        public Producer(AribaContractImportClient parent) {
            this.parent = parent;
            for (int i = 0; i < 10; i++) {
                Consumer consumer = new Consumer(this);
                consumer.setName("Consumer" + (i + 1));
                consumers.add(consumer);
                consumer.start();
                log.info("Consumer started:" + consumer.getName());
            }
        }

        @Override
        public void run() {
            while (true) {
                synchronized (parent) {
                    if (received.isEmpty()) {
                        try {
                            log.info("sleep...");
                            parent.wait();
                            log.info(">wake up...");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                while (received.iterator().hasNext()) {
                    try {
                        add(Lists.newArrayList(received.remove()));
                    } catch (ImportException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private synchronized ContractImportItem produce() {
            while (toProcess.isEmpty()) {
                try {
                    log.info("no items to process -> going to sleep");
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ContractImportItem item = toProcess.poll();
            log.info(String.format("item provided, %s left", toProcess.size()));
            return item;
        }

        public int getLeft() {
            int left = toProcess.size();
            for (Consumer consumer : producer.getConsumers()) {
                if (consumer.status == IN_PROGRESS) {
                    left++;
                }
            }
            return left;
        }

        public void addAsync(List<ContractImportItem> items) {
            received.addAll(items);
            synchronized (parent) {
                parent.notify();
            }
        }

        public synchronized void add(List<ContractImportItem> items) throws ImportException {
            for (ContractImportItem item : items) {
                if (!item.isLocked() && !toProcess.contains(item)) {
                    item.setStatus(LOCKED);
                    itemsRepository.save(item);
                    toProcess.add(item);
                    log.info(String.format("item %s flagged to be imported", item.getUniqueName()));
                }
            }
            if (!toProcess.isEmpty()) {
                try {
                    setSender();
                } catch (Exception e) {
                    throw new ImportException("Exception occured when setting the credentials");
                }
                notifyAll();
            }
        }

        public STATUS getStatus() {
            for (Consumer consumer : consumers) {
                if (consumer.status == IN_PROGRESS) {
                    return IN_PROGRESS;
                }
            }
            return IDLE;
        }

    }

    public class Consumer extends Thread {

        @Getter
        private STATUS status = IDLE;

        private final Producer producer;

        @Getter
        private ContractImportItem item;

        @Getter
        private Date lastUpdateTime = new Date();

        public Consumer(Producer producer) {
            Preconditions.checkNotNull(producer);
            this.producer = producer;
        }

        @Override
        public void run() {
            while (true) {
                item = producer.produce();
                status = IN_PROGRESS;
                lastUpdateTime = new Date();
                try {
                    importItem(item);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    item = null;
                    status = IDLE;
                    lastUpdateTime = new Date();
                }
            }
        }

        public void importItem(ContractImportItem item) {
            if (!item.isActive()) {
                return;
            }
            try {
                importHeader(item);
                if (item.getStatus() == HEADER_IMPORTED) {
                    try {
                        importDocuments(item);
                        item.setStatus(ALL_IMPORTED);
                    } catch (DocumentsImportException e) {
                        for (Map.Entry<ContractImportDocument, DocumentImportException> entry : e.getDocToExceptionMap().entrySet()) {
                            persistDocException(entry.getValue());
                        }
                    }
                }
            } catch (ImportException e) {
                persistItemException(e, item);
            } catch (Exception e) {
                persistItemException(e, item);
            } finally {
                // Determine the status of the item based on documents
                for (ContractImportDocument doc : item.getActiveDocuments()) {
                    if (doc.getStatus() == IMPORT_FAILED) {
                        item.setStatus(IMPORT_ATTACHMENTS_FAILED);
                        break;
                    }
                }
                itemsRepository.save(item);
            }
        }

        public void reset() {
            status = IDLE;
        }
    }

    private void persistItemException(Exception e, ContractImportItem item) {
        item.setStatus(IMPORT_HEADER_FAILED);
        item.setErrorMessage(formatException(e));
        log.error(String.format(String.format("Error encountered while importing contract %s -> %s", item.getUniqueName(), e.getMessage())));
    }

    private void persistDocException(DocumentImportException e) {
        ContractImportDocument doc = e.getDoc();
        doc.setErrorMessage(formatException(e));
        doc.setStatus(IMPORT_FAILED);
        documentsRepository.save(e.getDoc());
        log.error(String.format(String.format("Error encountered while importing document %s for contract %s -> %s", doc.getName(), doc.getContract(), e.getMessage())));
    }

    private String formatException(Exception e) {
        String ret = e.getMessage();
        if (ret == null) {
            ret = "No message provided";
        } else {
            if (ret.length() > 255) {
                ret = ret.substring(0, 254);
            }
        }
        return ret;
    }


    private void importHeader(ContractImportItem item) throws ImportException {
        if (!item.isActive()) {
            return;
        }
        item.setStatus(ContractImportItemStatus.IMPORTING_HEADER);
        item.setImportStartTime(new Date());
        item.setErrorMessage(null);
        itemsRepository.save(item);
        try {

            //We need to retrieve the parent item for SubAgreement (as we need to use the Generated Id from the parent instead of Regular Id)
            ContractImportItem parentItem = null;
            if (item.getHierarchicalType() == SubAgreement) {
                String parentAgreement = item.getData().getParentAgreement();
                parentItem = itemsRepository.findOne(parentAgreement);
                if (parentItem == null) {
                    throw new ImportException(String.format("Parent Agreement %s not found", parentAgreement));
                } else if (Strings.isNullOrEmpty(parentItem.getAribaId())) {
                    throw new ImportException(String.format("Parent Agreement %s not imported yet", parentAgreement));
                }

            }

            ContractWorkspaceImportRequest request = new WSObjectBuilder(configService.getConfig(), item, Optional.fromNullable(parentItem)).buildConractHeaderRequest();
            log.info(String.format("Importing contract %s", item.getUniqueName()));
            logHeaderRequest(request);

            ContractWorkspaceImportReply reply = (ContractWorkspaceImportReply) template.marshalSendAndReceive(configService.getContractImportURL(), request);
            ContractWorkspaceImportReply.WSContractWorkspaceOutputBeanItem replyItem = reply.getWSContractWorkspaceOutputBeanItem();

            WSContractWorkspaceOutputBean replyBean = replyItem.getItem();
            if (replyBean.getStatus().equals(OK)) {
                item.setImportFinishTime(new Date());
                item.setAribaId(replyBean.getWorkspaceId());
                item.setAribaWebJumper(replyBean.getUrl());
                item.setErrorMessage(null);
                item.setStatus(HEADER_IMPORTED);
                log.info(String.format("Contract %s imported with id %s", item.getUniqueName(), replyBean.getWorkspaceId()));
            } else {
                throw new ImportException(replyBean.getErrorMessage());
            }

        } catch (URISyntaxException e) {
            throw new ImportException(e.getCause());
        } catch (DatatypeConfigurationException e) {
            throw new ImportException(e.getCause());
        } catch (IOException e) {
            throw new ImportException(e.getCause());
        } catch (WebServiceTransportException e) {
            throw new ImportException(e.getMessage());
        } catch (Exception e) {
            throw new ImportException(e.getMessage());
        }

    }

    private void importDocuments(ContractImportItem item) throws DocumentsImportException {
        if (!item.isActive()) {
            return;
        }
        Config config = configService.getConfig();
        item.setStatus(IMPORTING_ATTACHMENTS);
        itemsRepository.save(item);

        ObjectFactory f = new ObjectFactory();
        FluentIterable<ContractImportDocument> docs = FluentIterable.from(item.getActiveDocuments()).filter(new Predicate<ContractImportDocument>() {
            @Override
            public boolean apply(ContractImportDocument contractImportDocument) {
                ContractImportDocumentStatus status = contractImportDocument.getStatus();
                return status == READY || status == IMPORT_FAILED;
            }
        });
        List<DocumentImportException> exceptions = Lists.newArrayList();
        for (ContractImportDocument doc : docs) {
            log.info(String.format("Importing Document %s for contract %s", doc.getName(), item.getUniqueName()));
            WSDocumentInputBean wsDocumentInputBean = f.createWSDocumentInputBean();
            wsDocumentInputBean.setAction(doc.getAribaId() == null ? Action.Create.name() : Action.Update.name());

            // Set the document Name
            String fileName = doc.getName();
            String folder = doc.getAribaFolder();
            String pathAndFile = null;
            if (folder != null) {
                pathAndFile = Joiner.on("/").join(folder, fileName);
            }
            wsDocumentInputBean.setDocumentName(pathAndFile);

            if (config.isUseDefaultOwner()) {
                wsDocumentInputBean.setOnBehalfUserId(config.getDefaultOwner());
                wsDocumentInputBean.setOnBehalfUserPasswordAdapter(config.getDefaultOwnerPasswordAdapter());
            } else {
                Contract contract = item.getData();
                wsDocumentInputBean.setOnBehalfUserId(contract.getOwner().getUniqueName());
                wsDocumentInputBean.setOnBehalfUserPasswordAdapter(contract.getOwner().getPasswordAdapter().get());
            }

            wsDocumentInputBean.setWorkspaceId(item.getAribaId());
            wsDocumentInputBean.setDocumentId(doc.getAribaId() == null ? "" : doc.getAribaId());

            try {
                Path storedFile = sealService.getStoredFile(doc.getHash());
                FileInputStream fin = new FileInputStream(storedFile.toFile());
                BufferedInputStream bis = new BufferedInputStream(fin);
                byte[] content = new byte[(int) storedFile.toFile().length()];
                bis.read(content);
                bis.close();
                fin.close();
                wsDocumentInputBean.setContents(content);

                DocumentImportRequest.WSDocumentInputBeanItem documentImportRequestWSDocumentInputBeanItem = f.createDocumentImportRequestWSDocumentInputBeanItem();
                documentImportRequestWSDocumentInputBeanItem.setItem(wsDocumentInputBean);

                DocumentImportRequest request = f.createDocumentImportRequest();
                logHeaderRequest(request);
                request.setWSDocumentInputBeanItem(documentImportRequestWSDocumentInputBeanItem);

                DocumentImportReply reply = (DocumentImportReply) template.marshalSendAndReceive(configService.getDocumentsImportURL(), request);
                DocumentImportReply.WSDocumentOutputBeanItem wsDocumentOutputBeanItem = reply.getWSDocumentOutputBeanItem();
                WSDocumentOutputBean replyBean = wsDocumentOutputBeanItem.getItem();
                if (replyBean.getStatus().equals(OK)) {
                    doc.setAribaId(replyBean.getDocumentId());
                    doc.setErrorMessage(null);
                    doc.setStatus(ContractImportDocumentStatus.IMPORTED);
                    doc.setImportFinishTime(new Date());
                    documentsRepository.save(doc);
                    log.info(String.format("Document %s imported with id %s", doc.getName(), replyBean.getDocumentId()));
                } else {
                    exceptions.add(new DocumentImportException(replyBean.getErrorMessage(), doc));
                }

            } catch (MalformedURLException e) {
                exceptions.add(new DocumentImportException(e, doc));
            } catch (IOException e) {
                exceptions.add(new DocumentImportException(e, doc));
            } catch (Exception e) {
                exceptions.add(new DocumentImportException(e, doc));
            } catch (OutOfMemoryError e) {
                exceptions.add(new DocumentImportException(e, doc));
            }
        }
        if (!exceptions.isEmpty()) {
            throw new DocumentsImportException(new Throwable(String.format("Documents failed to be imported for contract %s", item.getUniqueName())), exceptions);
        }
    }

    private void setSender() throws Exception {
        final Config config = configService.getConfig();
        HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender();
        CloseableHttpClient httpClient = HttpClients.custom().addInterceptorFirst(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
                HttpHost target = (HttpHost) httpContext.getAttribute(HttpClientContext.HTTP_TARGET_HOST);

                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()), new UsernamePasswordCredentials(config.getAribaUser(), config.getAribaPwd()));

                AuthState authState = (AuthState) httpContext.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
                authState.update(new BasicScheme(), credsProvider.getCredentials(new AuthScope(target.getHostName(), target.getPort())));

                httpRequest.removeHeaders("Content-Length");
            }
        }).build();
        messageSender.setHttpClient(httpClient);
        messageSender.afterPropertiesSet();
        template.setMessageSender(messageSender);
        template.afterPropertiesSet();
    }

    private void logHeaderRequest(Object request) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(request.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        try (Writer writer = new StringWriter()) {
            jaxbMarshaller.marshal(request, writer);
            writer.flush();
            log.debug("Request posted:\n {}", writer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
