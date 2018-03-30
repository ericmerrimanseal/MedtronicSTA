package com.seal.contracts.generator.ui.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seal.contracts.generator.csv.processor.DBDownloader;
import com.seal.contracts.generator.csv.processor.LinksGenerator;
import com.seal.contracts.generator.csv.processor.TeamGenerator;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import com.seal.contracts.generator.csv.service.ImportService;
import com.seal.contracts.ws.client.ariba.push.ImportException;
import com.seal.contracts.ws.client.ariba.push.AribaContractImportClient;
import com.seal.contracts.ws.client.seal.pull.SealPullProducer;
import lombok.Getter;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 17.08.15..
 */
@Controller
public class AdminController {

    @Autowired
    private LinksGenerator linksGenerator;

    @Autowired
    private TeamGenerator teamGenerator;

    @Autowired
    private DBDownloader dbDownloader;

    @Autowired
    private ImportService importService;

    @Autowired
    private ContractImportItemRepository itemRepository;

    @Autowired
    private ContractImportDocumentRepository documentRepository;

    @Autowired
    private SealPullProducer pullProducer;

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String configForm() {
        return "build/admin/index";
    }

    @RequestMapping(value = "/admin", method = RequestMethod.POST)
    public ModelAndView forceDiscovery() {
        ModelAndView mav = new ModelAndView("redirect:/builds/history/items/filter/status/ALL");
        pullProducer.getDB().clear();
        pullProducer.notifyThreads();
        return mav;
    }

    @RequestMapping(value = "/admin/download/team", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    private void downloadTeam(HttpServletResponse response) throws ImportException, IOException {

//        TeamGenerator teamGenerator = new TeamGenerator(itemsRepository, configService);
        File zip = teamGenerator.generate();
        response.setHeader("Content-Disposition", "attachment; filename=team.zip");
        InputStream is = new FileInputStream(zip);
        IOUtils.copy(is, response.getOutputStream());
        response.flushBuffer();
    }

    @RequestMapping(value = "/admin/download/db", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    private void downloadDB(HttpServletResponse response) throws ImportException, IOException {
        response.setHeader("Content-Disposition", "attachment; filename=db.zip");
        dbDownloader.generate(response.getOutputStream());
        response.flushBuffer();
    }

    @RequestMapping(value = "/admin/download/links", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    private void downloadLinks(HttpServletResponse response) throws ImportException, IOException {
        File zip = linksGenerator.generate();
        response.setHeader("Content-Disposition", "attachment; filename=links.zip");
        InputStream is = new FileInputStream(zip);
        IOUtils.copy(is, response.getOutputStream());
        response.flushBuffer();
        response.flushBuffer();
    }

    @RequestMapping(value = "/admin/review/import", method = RequestMethod.GET)
    private ModelAndView reviewImportProcess() throws ImportException, IOException {

        ModelAndView modelAndView = new ModelAndView("build/admin/importprocess");
        List<AribaContractImportClient.Consumer> consumers = importService.getConsumers();

        List<ConsumerInfo> infos = Lists.newArrayList();
        for (AribaContractImportClient.Consumer consumer : consumers) {
            ContractImportItem item = consumer.getItem();
            String uniqueName = item != null ? item.getUniqueName() : "";
            ConsumerInfo info = new ConsumerInfo(consumer, uniqueName, consumer.getLastUpdateTime());
            infos.add(info);
        }
        modelAndView.addObject("consumersInfo", infos);
        modelAndView.addObject("status", importService.getStatus());
        modelAndView.addObject("left", importService.getLeft());

        Map<String, Long> statesMap = Maps.newHashMap();
        List<Object[]> states = documentRepository.groupByStatus();
        for (Object[] state : states) {
            statesMap.put((String) state[0], new Long((Integer) state[1]));
        }

        modelAndView.addObject("states", statesMap);

        return modelAndView;
    }

    @RequestMapping(value = "/admin/review/import", method = RequestMethod.POST)
    private ModelAndView resetImportProcess(@ModelAttribute("info") ConsumerInfo info) {
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/review/import");
        info.getConsumer().reset();
        return modelAndView;
    }

    class ConsumerInfo {
        @Getter
        private final AribaContractImportClient.Consumer consumer;
        @Getter
        private final String contractId;
        @Getter
        private final Date lastChangedAt;

        public ConsumerInfo(AribaContractImportClient.Consumer consumer, String contractId, Date lastChangedAt) {
            this.consumer = consumer;
            this.contractId = contractId;
            this.lastChangedAt = lastChangedAt;
        }
    }

}
