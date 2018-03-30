package com.seal.contracts.generator.ui.controller;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.bean.User;
import com.seal.contracts.generator.csv.service.ConfigService;
import com.seal.contracts.generator.csv.service.UsersServiceImpl;
import com.seal.contracts.generator.persistence.entity.Config;
import com.seal.contracts.generator.persistence.repository.ConfigRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by root on 17.08.15..
 */
@Controller
public class ConfigController {

    private enum OPERATION {SAVE, UPLOAD_FILE}

    @Autowired
    private ConfigService configService;

    @Autowired
    private ConfigRepository configRepository;


    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public String configForm(Model model,
                             @ModelAttribute("config") final Config config) {

        if (config.getInFile() == null) {
            model.addAttribute("config", configService.getConfig());
        } else {
            model.addAttribute("config", config);
        }

        return "build/config/index";
    }

    @RequestMapping(value = {"/config"}, method = RequestMethod.POST)
    public ModelAndView configSave(@ModelAttribute Config config,
                                   @ModelAttribute(value = "params") String params,
                                   RedirectAttributes redirectAttributes) throws IOException {

        OPERATION op = OPERATION.valueOf(params.split(":")[0]);

        ModelAndView mav;
        switch (op) {

            case SAVE:

                if (Strings.isNullOrEmpty(config.getAribaPwd())) {
                    config.setAribaPwd(configService.getConfig().getAribaPwd());
                }
                if (Strings.isNullOrEmpty(config.getSealPassword())) {
                    config.setSealPassword(configService.getConfig().getSealPassword());
                }
                if (Strings.isNullOrEmpty(config.getRootFolder())) {
                    config.setRootFolder(configService.getConfig().getRootFolder());
                }

                List<Error> errors = validate(config);
                if (!errors.isEmpty()) {
                    redirectAttributes.addFlashAttribute("config", config);
                    redirectAttributes.addFlashAttribute("errors", errors);
                    mav = new ModelAndView("redirect:config");
                    return mav;
                }

                configRepository.save(config);
                mav = new ModelAndView("build/config/result");
                return mav;

            case UPLOAD_FILE:
                mav = new ModelAndView("redirect:/fileupload");
                redirectAttributes.addFlashAttribute("config", config);
                redirectAttributes.addFlashAttribute("params", params);
                return mav;
            default:
                throw new IllegalStateException("Unsupported Operation:" + params);
        }
    }

    private List<Error> validate(Config config) throws IOException {
        List<Error> result = Lists.newArrayList();

        result.addAll(validateFile(config.getInFile(), "Seal Export File"));

        List<Error> usersFileErrors = validateFile(config.getUsersFile(), "Users File");
        result.addAll(usersFileErrors);
        if (usersFileErrors.isEmpty()) {
            // Default User
            UsersServiceImpl usersService = new UsersServiceImpl(new File(config.getUsersFile()), new User(config.getDefaultOwner(), Optional.<String>absent()));
            Optional<User> userOptional = usersService.userByUniqueName(config.getDefaultOwner());
            if (!userOptional.isPresent()) {
                result.add(new Error("Default User", "does not exist"));
            }
        }

        result.addAll(validateFile(config.getEnumsFile(), "Enum File"));
        result.addAll(validateFile(config.getCommodityCodesFile(), "Commodities File"));

        return result;
    }

    private List<Error> validateFile(String path, String fieldName) {
        List<Error> result = Lists.newArrayList();
        if (Strings.isNullOrEmpty(path)) {
            result.add(new Error(fieldName, "must ot be empty"));
        } else {
            if (!new File(path).exists()) {
                result.add(new Error(fieldName, "does not exist"));
            }
        }
        return result;
    }

    public class Error {

        @Getter
        private final String field;

        @Getter
        private final String message;


        public Error(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String formatted() {
            return String.format("%s %s", field, message);
        }

    }
}


