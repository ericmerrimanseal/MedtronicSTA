package com.seal.contracts.generator.ui.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.seal.contracts.generator.csv.service.Json;
import com.seal.contracts.generator.html.JsonModel;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import com.seal.contracts.ws.client.seal.SealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * Created by root on 17.08.15..
 */
@Controller
@Slf4j
public class DetailController {

    @Autowired
    private ContractImportItemRepository itemsRepository;

    @Autowired
    private SealService sealService;

    @RequestMapping(value = "/builds/history/items/detail/{contractid}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable String contractid) {
        ModelAndView mav = new ModelAndView("build/history/items/detail");
        ContractImportItem item = itemsRepository.findOne(contractid);
        mav.addObject("item", item);
        return mav;
    }

    @RequestMapping(value = "/builds/history/items/detail/{documentId}/seal", method = RequestMethod.GET)
    public ModelAndView showInSeal(@PathVariable String documentId) throws URISyntaxException, JsonProcessingException, JSONException {
        Object result = sealService.getMetadata(documentId);
        log.info("retrieving metadata from Seal for document {}", documentId);
        ModelAndView mav = new ModelAndView("build/history/items/json");
        JsonModel jsonModel = new JsonModel(documentId, Json.JSON.toJson(result));
        mav.addObject("m", jsonModel);
        return mav;
    }

    @RequestMapping(value = "/builds/history/items/detail/{documentId}/seal/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadFromSeal(@PathVariable String documentId, HttpServletResponse response) throws URISyntaxException, IOException, JSONException {
        response.setHeader("Content-Disposition", "attachment; filename=seal_download");
        Path storedFile = sealService.getStoredFile(documentId);
        IOUtils.copy(new FileInputStream(storedFile.toFile()), response.getOutputStream());
        response.flushBuffer();
    }

}
