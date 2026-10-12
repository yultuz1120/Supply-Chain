-- AI 智慧供应链 Demo 增量表结构，可重复执行。
CREATE TABLE IF NOT EXISTS sc_store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '门店ID',
    name VARCHAR(64) NOT NULL COMMENT '门店名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='门店';

CREATE TABLE IF NOT EXISTS sc_courier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配送员ID',
    name VARCHAR(32) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    online_status TINYINT NOT NULL DEFAULT 1 COMMENT '1在线 0离线',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sc_courier_phone (phone)
) COMMENT='配送中心配送员';

CREATE TABLE IF NOT EXISTS sc_delivery_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配送单ID',
    delivery_number VARCHAR(40) NOT NULL COMMENT '配送单号',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    source_order_id BIGINT NULL COMMENT '来源业务订单ID',
    courier_id BIGINT NULL COMMENT '配送员ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1待接单 2已接单 3配送中 4已送达 5已取消',
    receiver_name VARCHAR(32) NULL,
    receiver_phone VARCHAR(20) NULL,
    delivery_address VARCHAR(255) NULL,
    urge_count INT NOT NULL DEFAULT 0 COMMENT '催单次数',
    last_urge_time DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_time DATETIME NULL,
    delivered_time DATETIME NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sc_delivery_number (delivery_number),
    UNIQUE KEY uk_sc_source_order (source_order_id),
    KEY idx_sc_delivery_store_status (store_id, status),
    KEY idx_sc_delivery_courier (courier_id)
) COMMENT='配送订单';

CREATE TABLE IF NOT EXISTS sc_urge_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    delivery_order_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    operator_id BIGINT NULL,
    source VARCHAR(16) NOT NULL DEFAULT 'PAGE' COMMENT 'PAGE或AI_MCP',
    remark VARCHAR(255) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_sc_urge_delivery (delivery_order_id)
) COMMENT='门店催单记录';

CREATE TABLE IF NOT EXISTS ai_conversation (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(128) NULL,
    user_id BIGINT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='AI会话';

CREATE TABLE IF NOT EXISTS ai_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id VARCHAR(64) NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    references_json TEXT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ai_message_conversation (conversation_id, create_time)
) COMMENT='AI消息';

CREATE TABLE IF NOT EXISTS ai_tool_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id VARCHAR(64) NULL,
    tool_name VARCHAR(64) NOT NULL,
    arguments_json TEXT NULL,
    result_summary TEXT NULL,
    success TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='AI工具调用日志';

INSERT INTO sc_store (id, name, status) VALUES
(1001, '华东一号门店', 1),
(1002, '华东二号门店', 1),
(1003, '社区生鲜门店', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sc_courier (id, name, phone, online_status) VALUES
(2001, '张骑手', '13800002001', 1),
(2002, '李骑手', '13800002002', 1),
(2003, '王骑手', '13800002003', 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sc_delivery_order
    (id, delivery_number, store_id, courier_id, status, receiver_name, receiver_phone, delivery_address, urge_count, create_time)
VALUES
    (3001, 'DL202610110001', 1001, NULL, 1, '陈女士', '13900001001', '上海市浦东新区演示路18号', 0, NOW()),
    (3002, 'DL202610110002', 1001, 2001, 3, '赵先生', '13900001002', '上海市闵行区供应链路66号', 1, DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
    (3003, 'DL202610110003', 1002, 2002, 2, '周女士', '13900001003', '上海市徐汇区智慧路8号', 0, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
    (3004, 'DL202610110004', 1003, 2001, 4, '孙先生', '13900001004', '上海市杨浦区农业路12号', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR))
ON DUPLICATE KEY UPDATE delivery_number = VALUES(delivery_number);
