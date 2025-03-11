-- 콘텐츠 데이터 삽입
INSERT INTO contents (title, description, genre, release_date, duration, content_url, created_at, updated_at)
VALUES ('Movie A', 'Action-packed movie A', 'Action', '2025-01-01', 7200, 'http://cdn.example.com/movieA.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO contents (title, description, genre, release_date, duration, content_url, created_at, updated_at)
VALUES ('Movie B', 'A fun comedy movie B', 'Comedy', '2025-02-15', 5400, 'http://cdn.example.com/movieB.mp4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 재생 이력 데이터 삽입 (userId는 인증 서비스에서 전달받은 사용자 ID)
INSERT INTO playback_histories (user_id, content_id, watched_at, duration_watched, rating)
VALUES (1, 1, CURRENT_TIMESTAMP, 3600, 4);

INSERT INTO playback_histories (user_id, content_id, watched_at, duration_watched, rating)
VALUES (1, 2, CURRENT_TIMESTAMP, 2700, 5);
