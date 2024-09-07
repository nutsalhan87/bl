package ru.itmo.hub;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.core.model.comments.comment.Comment;
import ru.itmo.hub.core.model.comments.comment.CommentRepository;
import ru.itmo.hub.core.model.comments.commentreport.CommentReport;
import ru.itmo.hub.core.model.comments.commentreport.CommentReportRepository;
import ru.itmo.hub.service.VideoService;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public class CommentModerationTests {
    private final CommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;
    private final MockMvc mvc;

    @Autowired
    public CommentModerationTests(CommentRepository commentRepository,
                                  CommentReportRepository commentReportRepository,
                                  MockMvc mvc) {
        this.commentRepository = commentRepository;
        this.commentReportRepository = commentReportRepository;
        this.mvc = mvc;
    }

    @BeforeAll
    static void createData(@Autowired VideoService videoService,
                           @Autowired CommentRepository commentRepository,
                           @Autowired CommentReportRepository commentReportRepository) {
        var vadim = "vadim";

        var video = VideoDto.builder().id(1L).name("Vadim").authorName(vadim).description("Aboba").build();
        videoService.createVideo(video, vadim);

        var comment = new Comment(null, video.getId(), vadim, Timestamp.from(Instant.now()), "Oh shit, I'm sorry!");
        commentRepository.save(comment);

        var commentReport = new CommentReport(comment);
        commentReportRepository.save(commentReport);
    }

    @Test
    void uncheckedComments() throws Exception {
        mvc.perform(get("/moderation/comment/list")).
                andExpect(MockMvcResultMatchers.jsonPath("$.[*].id").
                        value(Matchers.contains(1)));
    }

    @Test
    void approveComment() throws Exception {
        var comment = commentRepository.findById(1L).orElseThrow();
        assertTrue(commentReportRepository.existsByComment(comment));
        mvc.perform(patch("/moderation/comment/approve/1")).
                andExpect(MockMvcResultMatchers.status().isNoContent());
        assertFalse(commentReportRepository.existsByComment(comment));
        assertTrue(commentRepository.existsById(1L));

        mvc.perform(patch("/moderation/comment/approve/10")).
                andExpect(MockMvcResultMatchers.status().isNotFound()).
                andExpect(MockMvcResultMatchers.content().string("Comment doesn't exist."));
    }

    @Test
    void deleteComment() throws Exception {
        var comment = commentRepository.findById(1L).orElseThrow();
        assertTrue(commentReportRepository.existsByComment(comment));
        mvc.perform(delete("/moderation/comment/delete/1")).
                andExpect(MockMvcResultMatchers.status().isNoContent());
        assertFalse(commentReportRepository.existsByComment(comment));
        assertFalse(commentRepository.existsById(1L));

        mvc.perform(delete("/moderation/comment/delete/10")).
                andExpect(MockMvcResultMatchers.status().isNotFound()).
                andExpect(MockMvcResultMatchers.content().string("Comment doesn't exist."));
    }
}
