package com.seal.contracts.generator.ui.controller;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.seal.contracts.generator.csv.bean.Contract;
import com.seal.contracts.generator.csv.service.ConfigService;
import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportDocumentStatus;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import com.seal.contracts.generator.csv.service.ImportService;
import com.seal.contracts.ws.client.ariba.push.ImportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.seal.contracts.generator.csv.bean.Contract.HIERARCHICAL_TYPE.*;

/**
 * Created by root on 17.08.15..
 */
@Controller
public class HistoryController {

    @Autowired
    private ContractImportItemRepository itemsRepository;

    @Autowired
    private ContractImportDocumentRepository documentRepository;

    @Autowired
    private ImportService importService;

    @Autowired
    private ConfigService configService;

    @RequestMapping(value = "/builds/history/items", method = RequestMethod.GET)
    public ModelAndView show() {
        Iterable<ContractImportItem> items = itemsRepository.findAll();
        return itemsView(items);
    }

    @RequestMapping(value = "/builds/history/items/**", method = RequestMethod.POST)
    public ModelAndView submit(@ModelAttribute(value = "query") String query) throws ImportException {
        return importMany(query);
    }

    @RequestMapping(value = "/builds/history/items/filter/status/{status}", method = RequestMethod.GET)
    public ModelAndView showItemsByStatus(Model model, @PathVariable String status) {

        Iterable<ContractImportItem> items;

        if ("ALL".equals(status)) {
            items = itemsRepository.findAll();
        } else {
            Iterable<ContractImportDocument> docs = documentRepository.findByStatus(ContractImportDocumentStatus.valueOf(status));
            ImmutableSet<String> contractIds = FluentIterable.from(docs).transform(new Function<ContractImportDocument, String>() {
                @Override
                public String apply(ContractImportDocument contractImportDocument) {
                    return contractImportDocument.getContract();
                }
            }).toSet();

            items = itemsRepository.findAll(contractIds);
        }

        return itemsView(items);
    }

    @RequestMapping(value = "/builds/history/items/filter/hierarchicaltype/{type}", method = RequestMethod.GET)
    public ModelAndView showItemsByHierarchicalType(Model model, @PathVariable String type) {

        List<ContractImportItem> items;

        if ("STANDALONE_AND_MASTER".equals(type)) {
            items = itemsRepository.findByHierarchicalType(StandAlone);
            items.addAll(itemsRepository.findByHierarchicalType(MasterAgreement));
        } else {
            items = itemsRepository.findByHierarchicalType(Contract.HIERARCHICAL_TYPE.valueOf(type));
        }

        return itemsView(items);
    }

    private ModelAndView itemsView(Iterable<ContractImportItem> items) {

        ImmutableList<ContractImportItem> sortedItems = FluentIterable.from(items).toSortedList(new Comparator<ContractImportItem>() {
            @Override
            public int compare(ContractImportItem o1, ContractImportItem o2) {
                return o2.getGeneratedTime().compareTo(o1.getGeneratedTime());
            }
        });

        ModelAndView modelAndView = new ModelAndView("build/history/items/index");
        modelAndView.addObject("items", sortedItems);
        modelAndView.addObject("status", importService.getStatus());
        modelAndView.addObject("left", importService.getLeft());

        Map<String, Long> statesMap = Maps.newLinkedHashMap();
        statesMap.put("ALL", documentRepository.count());


        List<Object[]> states = documentRepository.groupByStatus();
        for (Object[] state : states) {
            statesMap.put((String) state[0], new Long((Integer) state[1]));
        }

        modelAndView.addObject("states", statesMap);
        Map<String, Integer> hierarchyTypesMap = Maps.newLinkedHashMap();

        Iterable<ContractImportItem> all = itemsRepository.findAll();
        ImmutableMap<Contract.HIERARCHICAL_TYPE, Collection<ContractImportItem>> byHierarchy = Multimaps.index(all, new Function<ContractImportItem, Contract.HIERARCHICAL_TYPE>() {
            @Override
            public Contract.HIERARCHICAL_TYPE apply(ContractImportItem item) {
                return item.getHierarchicalType();
            }
        }).asMap();

        for (Map.Entry<Contract.HIERARCHICAL_TYPE, Collection<ContractImportItem>> entry : byHierarchy.entrySet()) {
            hierarchyTypesMap.put(entry.getKey().name(), entry.getValue().size());
        }

        int mastersSize = Optional.fromNullable(byHierarchy.get(Contract.HIERARCHICAL_TYPE.MasterAgreement)).or(Lists.newArrayList()).size();
        int standAloneSize = Optional.fromNullable(byHierarchy.get(Contract.HIERARCHICAL_TYPE.StandAlone)).or(Lists.newArrayList()).size();

        hierarchyTypesMap.put("STANDALONE_AND_MASTER", mastersSize + standAloneSize);
        modelAndView.addObject("hierarchicaltypes", hierarchyTypesMap);

        return modelAndView;
    }

    @RequestMapping(value = "/builds/history/item/{itemid}/import", method = RequestMethod.GET)
    public ModelAndView importItem(Model model, @PathVariable String itemid) throws ImportException {
        return importMany(itemid);
    }

    @RequestMapping(value = "/builds/history/items/import/{query}", method = RequestMethod.GET)
    public ModelAndView importMany(@PathVariable String query) throws ImportException {

        List<ContractImportItem> items = Lists.newArrayList();
        if ("STANDALONE_AND_MASTER".equals(query)) {
            items.addAll(itemsRepository.findByHierarchicalType(StandAlone));
            items.addAll(itemsRepository.findByHierarchicalType(MasterAgreement));
        } else if (StandAlone.name().equals(query)) {
            items.addAll(itemsRepository.findByHierarchicalType(StandAlone));
        } else if (MasterAgreement.name().equals(query)) {
            items.addAll(itemsRepository.findByHierarchicalType(MasterAgreement));
        } else if (SubAgreement.name().equals(query)) {
            items.addAll(itemsRepository.findByHierarchicalType(SubAgreement));
        } else if ("ALL".equals(query)) {
            items.addAll(Lists.newArrayList(itemsRepository.findAll()));
        } else if (!Strings.isNullOrEmpty(query)) {
            String itemId = query;
            items.add(itemsRepository.findOne(itemId));
        }
        return importItems(items);
    }

    private ModelAndView importItems(List<ContractImportItem> items) throws ImportException {

        ModelAndView mav = new ModelAndView("redirect:/builds/history/items/filter/status/ALL");

        // Remove inactive ones
        List<ContractImportItem> filtered = items.stream().filter(item -> item.getStatus().canImport() && item.isActive()).sorted(new Comparator<ContractImportItem>() {
            @Override
            public int compare(ContractImportItem i1, ContractImportItem i2) {
                return i1.getGeneratedTime().compareTo(i2.getGeneratedTime());
            }
        }).collect(Collectors.toList());

        importService.add(Lists.newArrayList(filtered));

        return mav;
    }
}
