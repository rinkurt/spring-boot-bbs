package com.herokuapp.ddspace.controller;

import com.herokuapp.ddspace.cache.HotTagCache;
import com.herokuapp.ddspace.dto.PaginationDTO;
import com.herokuapp.ddspace.dto.QuestionDTO;
import com.herokuapp.ddspace.mapper.UserMapper;
import com.herokuapp.ddspace.service.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@AllArgsConstructor
public class IndexController {

    private QuestionService questionService;
    private HotTagCache hotTagCache;

    @GetMapping("/")
    public String index(@RequestParam(name = "page", defaultValue = "1") Integer page,
                        @RequestParam(name = "size", defaultValue = "10") Integer size,
                        @RequestParam(name = "search", required = false) String search,
                        Model model) {

        PaginationDTO<QuestionDTO> paginationDTO = questionService.list(search, page, size);
        model.addAttribute("pagination", paginationDTO);
        model.addAttribute("search", search);
        model.addAttribute("hotTagList", hotTagCache.getHotTagDTOList());
        return "index";
    }
}