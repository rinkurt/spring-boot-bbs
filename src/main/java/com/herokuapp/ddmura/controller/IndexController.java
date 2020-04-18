package com.herokuapp.ddmura.controller;

import com.herokuapp.ddmura.cache.HotTagCache;
import com.herokuapp.ddmura.dto.PaginationDTO;
import com.herokuapp.ddmura.dto.QuestionDTO;
import com.herokuapp.ddmura.service.HotTagService;
import com.herokuapp.ddmura.service.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class IndexController {

    private QuestionService questionService;
    private HotTagService hotTagService;
    private HotTagCache hotTagCache;

    @GetMapping("/")
    public String index(@RequestParam(name = "page", defaultValue = "1") Integer page,
                        @RequestParam(name = "size", defaultValue = "10") Integer size,
                        @RequestParam(name = "search", required = false) String search,
                        Model model) {

        hotTagService.getHotTags();

        PaginationDTO<QuestionDTO> paginationDTO = questionService.list(search, page, size);
        model.addAttribute("pagination", paginationDTO);
        model.addAttribute("search", search);
        model.addAttribute("hotTagList", hotTagCache.getHotTagDTOList());
        return "index";
    }
}