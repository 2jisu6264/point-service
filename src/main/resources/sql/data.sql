INSERT INTO point_policy VALUES
                             ('POINT_EARN_MIN', 1, '1회 최소 적립 포인트'),
                             ('POINT_EARN_MAX', 100000, '1회 최대 적립 포인트'),
                             ('POINT_BALANCE_MAX', 500000, '회원 보유 최대 무료 포인트');
INSERT INTO member (
    external_member_id,
    member_name,
    point_balance,
    created_at
) VALUES
      ('EXT_USER_001', '홍길동', 10000, '20260108'),
      ('EXT_USER_002', '김철수', 5000,  '20260108');
INSERT INTO order_sequence_log (
    order_date,
    seq,
    created_at
) VALUES
      ('20250809', 0001,  CURRENT_TIMESTAMP),
      ('20250810', 0002,  CURRENT_TIMESTAMP);
