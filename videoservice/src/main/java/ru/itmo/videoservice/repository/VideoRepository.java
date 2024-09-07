package ru.itmo.videoservice.repository;

import lombok.Locked;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.jcr2dav.Jcr2davRepositoryFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.itmo.shared.Video;

import javax.jcr.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class VideoRepository implements DisposableBean {
    private final Session session;

    @Autowired
    public VideoRepository(@Value("${jackrabbit.uri}") String uri,
                           @Value("${jackrabbit.username}") String username,
                           @Value("${jackrabbit.password}") String password) throws RepositoryException {
        var jcr2davRepositoryFactory = new Jcr2davRepositoryFactory();
        var params = Map.of("org.apache.jackrabbit.repository.uri", uri);
        Repository repository;
        repository = jcr2davRepositoryFactory.getRepository(params);
        session = repository.login(new SimpleCredentials(username, password.toCharArray()));

        var root = session.getRootNode();
        try {
            root.getNode("videosInfo");
        } catch (PathNotFoundException e) {
            var videosInfo = root.addNode("videosInfo");
            videosInfo.setProperty("lastId", 0L);
        }
        try {
            root.getNode("videos");
        } catch (PathNotFoundException e) {
            root.addNode("videos");
        }
    }

    @SneakyThrows
    @Locked.Write
    public Long generateId() {
        long generated;
        try {
            var root = session.getRootNode();
            var videosInfoNode = root.getNode("videosInfo");
            var lastIdProperty = videosInfoNode.getProperty("lastId");
            generated = lastIdProperty.getLong() + 1;
            lastIdProperty.setValue(generated);
            session.save();
        } catch (Throwable t) {
            session.refresh(false);
            log.error(t.getMessage());
            throw t;
        }
        return generated;
    }

    @SneakyThrows
    @Locked.Read
    public List<Video> findAll() {
        var root = session.getRootNode();
        var videosNode = root.getNode("videos");
        return Utils.nodeIteratorToStream(videosNode.getNodes())
                .map(videoNode -> {
                    try {
                        return Utils.videoFromNode(videoNode);
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    @SneakyThrows
    @Locked.Read
    public Optional<Video> findById(Long id) {
        var root = session.getRootNode();
        var videosNode = root.getNode("videos");
        Video video;
        try {
            video = Utils.videoFromNode(videosNode.getNode(id.toString()));
        } catch (PathNotFoundException ignored) {
            video = null;
        }
        return Optional.ofNullable(video);
    }

    @SneakyThrows
    @Locked.Write
    public Long save(Video video) {
        try {
            var root = session.getRootNode();
            var videosNode = root.getNode("videos");
            Long videoId = video.getId() == null ? generateId() : video.getId();
            var videoNode = Utils.getOrAddNode(videosNode, videoId.toString());
            videoNode.setProperty("name", video.getName());
            videoNode.setProperty("description", video.getDescription());
            videoNode.setProperty("authorName", video.getAuthorName());
            videoNode.setProperty("views", video.getViews());
            videoNode.setProperty("posted", video.getPosted().toString());

            var tagIdsNode = Utils.getOrAddNode(videoNode, "tagIds");
            Utils.removeSubNodes(tagIdsNode);
            video.getTagIds().forEach(id -> {
                try {
                    tagIdsNode.addNode(id.toString());
                } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                }
            });

            var reactionIdsNode = Utils.getOrAddNode(videoNode, "reactionIds");
            Utils.removeSubNodes(reactionIdsNode);
            video.getReactionIds().forEach(id -> {
                try {
                    reactionIdsNode.addNode(id.toString());
                } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                }
            });

            session.save();

            return videoId;
        } catch (Throwable t) {
            session.refresh(false);
            log.error(t.getMessage());
            throw t;
        }
    }

    @SneakyThrows
    @Locked.Write
    public void delete(Long videoId) {
        try {
            var root = session.getRootNode();
            var videosNode = root.getNode("videos");
            var videoNode = videosNode.getNode(videoId.toString());
            videoNode.remove();
            session.save();
        } catch (Throwable t) {
            session.refresh(false);
            log.error(t.getMessage());
            throw t;
        }
    }

    @Override
    public void destroy() {
        session.logout();
    }

    private static class Utils {
        public static Node getOrAddNode(Node parentNode, String nodeName) {
            try {
                return parentNode.getNode(nodeName);
            } catch (PathNotFoundException ignored) {
                try {
                    return parentNode.addNode(nodeName);
                } catch (RepositoryException exc) {
                    throw new RuntimeException(exc);
                }
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }

        public static Stream<Node> nodeIteratorToStream(NodeIterator nodeIterator) {
            var streamBuilder = Stream.<Node>builder();
            while (nodeIterator.hasNext()) {
                streamBuilder.add(nodeIterator.nextNode());
            }
            return streamBuilder.build();
        }

        public static void removeSubNodes(Node node) throws RepositoryException {
            nodeIteratorToStream(node.getNodes()).forEach(subNode -> {
                try {
                    subNode.remove();
                } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public static Video videoFromNode(Node videoNode) throws RepositoryException {
            var id = Long.parseLong(videoNode.getName());
            var name = videoNode.getProperty("name").getString();
            var description = videoNode.getProperty("description").getString();
            var authorName = videoNode.getProperty("authorName").getString();
            var views = videoNode.getProperty("views").getLong();
            var posted = Timestamp.valueOf(videoNode.getProperty("posted").getString());

            var tagNodes = videoNode.getNode("tagIds").getNodes();
            var tagIds = nodeIteratorToStream(tagNodes).
                    map(node -> {
                        try {
                            return Long.parseLong(node.getName());
                        } catch (RepositoryException e) {
                            throw new RuntimeException(e);
                        }
                    }).
                    collect(Collectors.toSet());

            var reactionNodes = videoNode.getNode("reactionIds").getNodes();
            var reactionIds = nodeIteratorToStream(reactionNodes).
                    map(node -> {
                        try {
                            return Long.parseLong(node.getName());
                        } catch (RepositoryException e) {
                            throw new RuntimeException(e);
                        }
                    }).
                    collect(Collectors.toSet());

            return Video.builder().
                    id(id).
                    name(name).
                    description(description).
                    authorName(authorName).
                    views(views).
                    posted(posted).
                    tagIds(tagIds).
                    reactionIds(reactionIds).
                    build();
        }
    }
}
