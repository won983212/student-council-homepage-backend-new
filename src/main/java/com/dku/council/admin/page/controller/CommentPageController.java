package com.dku.council.admin.page.controller;

import com.dku.council.admin.page.service.CommentPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/manage/comment")
@RequiredArgsConstructor
public class CommentPageController {
    private final CommentPageService service;
    @PostMapping("/{commentId}/delete")
    public String deActivate(HttpServletRequest request, @PathVariable Long commentId){
        service.delete(commentId);
        return "redirect:" + request.getHeader("Referer");
    }
    @PostMapping("/{commentId}/activate")
    public String activeComment(HttpServletRequest request, @PathVariable Long commentId){
        service.active(commentId);
        return "redirect:" + request.getHeader("Referer");
    }

}
