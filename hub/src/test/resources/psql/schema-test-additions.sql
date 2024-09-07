\c hub_primary
CREATE OR REPLACE VIEW vids AS
SELECT
    v.id AS id,
    v.name AS name,
    v.views AS views,
    v.posted as posted,
    STRING_AGG(t.value, ', ') AS tags,
    (select count(*) from (select distinct hub_user_id from auth_user_view where video_id = v.id)) as unique_views
FROM
    public.video v
JOIN
    public.video_tag vt ON v.id = vt.video_id
JOIN
    public.tag t ON vt.tags_id = t.id
GROUP BY
    v.id, v.name
ORDER BY
    v.id;
