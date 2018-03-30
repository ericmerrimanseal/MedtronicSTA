package com.seal.contracts.generator.ui.controller;

import com.seal.contracts.generator.persistence.entity.Config;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by root on 17.08.15..
 */
@Controller
public class FileUploadController {

    public enum ACTION {SAVE, CANCEL}

    public enum FIELD {IN_FILE, USERS_FILE, SUPPLIERS_FILE, ENUMERATION_FILE, COMMODITY_CODE_FILE}

    private Config config;
    private FIELD field;
    private ModelAndView mav = new ModelAndView("redirect:config");


    @RequestMapping(value = "/fileupload", method = RequestMethod.GET)
    public String configForm(@ModelAttribute("config") Config config,
                             @ModelAttribute(value = "params") String params) {
        this.config = config;
        this.field = FIELD.valueOf(params.split(":")[1]);
        return "build/fileupload/index";
    }

    @RequestMapping(value = {"/fileupload"}, method = RequestMethod.POST)
    public ModelAndView upload(@RequestParam("file") MultipartFile file,
                               @RequestParam(value = "action", required = true) ACTION action,
                               RedirectAttributes redirectAttributes) throws IOException {

        redirectAttributes.addFlashAttribute("config", config);

        switch (action) {
            case SAVE:
                save(file);
                return mav;
            case CANCEL:
                return mav;
        }

        throw new IllegalStateException("Unsupported operation" + action.name());
    }


    private void save(MultipartFile file) throws IOException {
        File uploadFolder = new File("./in/upload");
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
        byte[] bytes = file.getBytes();

        String originalFileName = file.getOriginalFilename();

        Path path = Paths.get(uploadFolder.getPath(),
                String.format("%s_%s.%s",
                        com.google.common.io.Files.getNameWithoutExtension(originalFileName),
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()),
                        com.google.common.io.Files.getFileExtension(originalFileName)));
        Files.write(path, bytes);

        switch (field) {
            case IN_FILE:
                config.setInFile(path.toFile().getPath());
                break;
            case USERS_FILE:
                config.setUsersFile(path.toFile().getPath());
                break;
            case SUPPLIERS_FILE:
                config.setSupplierProfileFile(path.toFile().getPath());
                break;
            case ENUMERATION_FILE:
                config.setEnumsFile(path.toFile().getPath());
                break;
            case COMMODITY_CODE_FILE:
                config.setCommodityCodesFile(path.toFile().getPath());
                break;
            default:
                throw new IllegalStateException(String.format("Field %s is not supported", field.name()));

        }


    }
}
