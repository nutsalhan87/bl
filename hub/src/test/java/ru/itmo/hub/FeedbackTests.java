package ru.itmo.hub;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.core.model.comments.comment.Comment;
import ru.itmo.hub.core.model.comments.comment.CommentRepository;
import ru.itmo.hub.core.model.comments.commentreport.CommentReportRepository;
import ru.itmo.hub.core.model.primary.reaction.Reaction;
import ru.itmo.hub.core.model.primary.reaction.ReactionRepository;
import ru.itmo.hub.service.VideoService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public class FeedbackTests {
    private final CommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;
    private final ReactionRepository reactionRepository;
    private final MockMvc mvc;

    @Autowired
    public FeedbackTests(CommentRepository commentRepository,
                         CommentReportRepository commentReportRepository,
                         ReactionRepository reactionRepository,
                         MockMvc mvc) {
        this.commentRepository = commentRepository;
        this.commentReportRepository = commentReportRepository;
        this.reactionRepository = reactionRepository;
        this.mvc = mvc;
    }

    @BeforeAll
    static void createData(@Autowired VideoService videoService,
                           @Autowired ReactionRepository reactionRepository,
                           @Autowired CommentRepository commentRepository) {
        var vadim = "vadim";
        var videos = new VideoDto[]{
                VideoDto.builder().id(1L).name("Vadim").description("Aboba").authorName(vadim).build(),
                VideoDto.builder().id(2L).name("Vadim 2").description("Aboba").authorName(vadim).build(),
                VideoDto.builder().id(3L).name("Vadim 3").description("Aboba").authorName(vadim).build(),
        };
        for (var video : videos) {
            videoService.createVideo(video, vadim);
        }

        var comments = new Comment[]{
                new Comment(null, videos[0].getId(), vadim, null, "Oh shit, I'm sorry!"),
                new Comment(null, videos[0].getId(), vadim, null, "Sorry for what?"),
        };
        IntStream.range(0, comments.length).forEach((i) -> {
            Timestamp timestamp = Timestamp.from(Instant.now().minusSeconds(i));
            comments[i].setCreationTimestamp(timestamp);
        });
        commentRepository.saveAll(Arrays.asList(comments));

        var reactions = new Reaction[]{
                new Reaction(null, videos[0].getId(), vadim, true),
                new Reaction(null, videos[1].getId(), vadim, false)
        };
        reactionRepository.saveAll(Arrays.asList(reactions));
    }

    @Test
    @WithMockUser(username = "tarkin", password = "holocron")
    void comments() throws Exception {
        for (String s : Arrays.asList("/feedback/1/comment/list",
                "/feedback/1/comment/list?sorting=Newer",
                "/feedback/1/comment/list?sorting=NEVER")) {
            mvc.perform(get(s)).
                    andExpect(MockMvcResultMatchers.jsonPath("$.[*].id").value(Matchers.containsInRelativeOrder(1, 2))).
                    andExpect(MockMvcResultMatchers.jsonPath("$.[*].value").value(
                            Matchers.containsInRelativeOrder("Oh shit, I'm sorry!", "Sorry for what?")
                    ));
        }
        mvc.perform(get("/feedback/1/comment/list?sorting=OLdER")).
                andExpect(MockMvcResultMatchers.jsonPath("$.[*].id").value(Matchers.containsInRelativeOrder(2, 1))).
                andExpect(MockMvcResultMatchers.jsonPath("$.[*].value").value(
                        Matchers.containsInRelativeOrder("Sorry for what?", "Oh shit, I'm sorry!")
                ));
        mvc.perform(get("/feedback/10/comment/list")).
                andExpect(MockMvcResultMatchers.status().isNotFound()).
                andExpect(MockMvcResultMatchers.content().string(String.format("Video with id %s not found.", 10)));
    }

    @Test
    @WithMockUser(username = "tarkin", password = "holocron")
    void comment() throws Exception {
        mvc.perform(post("/feedback/2/comment/create").
                        content("My daddy...")).
                andExpect(MockMvcResultMatchers.status().isNoContent());
        assertTrue(commentRepository
                .findAllByVideoId(2L)
                .stream()
                .map(Comment::getValue)
                .toList()
                .contains("My daddy..."));

        mvc.perform(post("/feedback/4/comment/create").
                        content("0xDEAD")).
                andExpect(MockMvcResultMatchers.status().isNotFound()).
                andExpect(MockMvcResultMatchers.content().string(String.format("Video with id %s not found.", 4)));
    }

    @Test
    @WithMockUser(username = "tarkin", password = "holocron")
    void report() throws Exception {
        var comment = commentRepository.findById(1L).orElseThrow();
        assertFalse(commentReportRepository.existsByComment(comment));
        mvc.perform(put("/feedback/report/comment/1")).
                andExpect(MockMvcResultMatchers.status().isNoContent());
        assertTrue(commentReportRepository.existsByComment(comment));

        mvc.perform(put("/feedback/report/comment/10")).
                andExpect(MockMvcResultMatchers.status().isNotFound()).
                andExpect(MockMvcResultMatchers.content().string("Comment doesn't exist."));
    }

    @Test
    @WithMockUser(username = "tarkin", password = "holocron")
    void like() throws Exception {
        var vadim = "vadim";
        var videoId = 2L;

        assertFalse(reactionRepository.findReactionByVideoIdAndAuthorName(videoId, vadim).orElseThrow().isPositive());
        mvc.perform(put("/feedback/2/like")).
                andExpect(MockMvcResultMatchers.status().isNoContent());
        assertTrue(reactionRepository.findReactionByVideoIdAndAuthorName(videoId, vadim).orElseThrow().isPositive());

        mvc.perform(put("/feedback/5/like")).
                andExpect(MockMvcResultMatchers.status().isNotFound()).
                andExpect(MockMvcResultMatchers.content().string(String.format("Video with id %s not found.", 5)));
    }

    @Test
    @WithMockUser(username = "tarkin", password = "holocron")
    void dislike() throws Exception {
        var vadim = "vadim";
        var videoId = 1L;

        assertTrue(reactionRepository.findReactionByVideoIdAndAuthorName(videoId, vadim).orElseThrow().isPositive());
        mvc.perform(put("/feedback/1/dislike")).
                andExpect(MockMvcResultMatchers.status().isNoContent());
        assertFalse(reactionRepository.findReactionByVideoIdAndAuthorName(videoId, vadim).orElseThrow().isPositive());

        mvc.perform(put("/feedback/5/dislike")).
                andExpect(MockMvcResultMatchers.status().isNotFound()).
                andExpect(MockMvcResultMatchers.content().string(String.format("Video with id %s not found.", 5)));
    }

    @Test
    @WithMockUser(username = "tarkin", password = "holocron")
    void unreact() throws Exception {
        var vadim = "vadim";
        var videoId = 1L;

        assertTrue(reactionRepository.findReactionByVideoIdAndAuthorName(videoId, vadim).isPresent());
        mvc.perform(delete("/feedback/1/unreact")).
                andExpect(MockMvcResultMatchers.status().isNoContent());
        assertTrue(reactionRepository.findReactionByVideoIdAndAuthorName(videoId, vadim).isEmpty());

        mvc.perform(delete("/feedback/5/unreact")).
                andExpect(MockMvcResultMatchers.status().isNotFound()).
                andExpect(MockMvcResultMatchers.content().string(String.format("Video with id %s not found.", 5)));
    }
}
