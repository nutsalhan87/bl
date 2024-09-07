\c hub_primary
INSERT INTO video (id, views, name, posted)
SELECT
    ROW_NUMBER() OVER () AS id,
    ROUND(RANDOM() * 1000000) AS views,
    'Video ' || generate_series AS name,
    ( NOW() - INTERVAL '1 month' * random()) AS posted
FROM generate_series(1, 50);

INSERT INTO tag (id, value)
SELECT
    ROW_NUMBER() OVER () AS id,
    'Tag ' || generate_series AS value
FROM generate_series(1, 5);

INSERT INTO video_tag(tags_id, video_id)
SELECT distinct
    floor(random() * 5) + 1 AS tag_id,
    floor(random() * 50) + 1 AS video_id
FROM generate_series(1, 150);

-- че тут 1 запись Vadim делает вообще?
\c hub_auth
INSERT INTO hub_user (id, name)
SELECT
    generate_series,
    'User' || generate_series
FROM generate_series(2, 300);

\c hub_primary
INSERT INTO auth_user_view (id, hub_user_id, video_id, timestamp)
SELECT distinct
    generate_series as id,
    floor(random() * 298) + 2 AS hub_user_id,
    floor(random() * 50) + 1 AS video_id,
    NOW() - (interval '1' day * (RANDOM() * 15)::int) as timestamp
FROM generate_series(1, 900);
