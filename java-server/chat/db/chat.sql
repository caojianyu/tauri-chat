/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : localhost:3306
 Source Schema         : chat

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 17/01/2023 00:33:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for uc_account
-- ----------------------------
DROP TABLE IF EXISTS `uc_account`;
CREATE TABLE `uc_account`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账户',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '加密盐',
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of uc_account
-- ----------------------------
INSERT INTO `uc_account` VALUES (1, 'cjy11', '49f561efb400aef4f5413dab30f5cb56', 'CVbwThfbq6UFkQOLjVv6', '994ay', 'https://img0.baidu.com/it/u=2161795608,1145691066&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500', '2023-01-10 20:37:10');
INSERT INTO `uc_account` VALUES (2, 'cjy12', '49f561efb400aef4f5413dab30f5cb56', 'CVbwThfbq6UFkQOLjVv6', '1212', 'https://img0.baidu.com/it/u=4163049905,243564169&fm=253&fmt=auto&app=138&f=JPEG?w=511&h=500', '2023-01-10 20:37:10');
INSERT INTO `uc_account` VALUES (3, 'cjy13', '49f561efb400aef4f5413dab30f5cb56', 'CVbwThfbq6UFkQOLjVv6', '1313', 'https://img0.baidu.com/it/u=5061270,3548933205&fm=253&fmt=auto&app=138&f=JPEG?w=501&h=500', '2023-01-10 20:37:10');

-- ----------------------------
-- Table structure for uc_account_relation
-- ----------------------------
DROP TABLE IF EXISTS `uc_account_relation`;
CREATE TABLE `uc_account_relation`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `account_id` int NULL DEFAULT NULL COMMENT '用户id（发起人）',
  `friend_id` int NULL DEFAULT NULL COMMENT '好友id（被接收人）',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of uc_account_relation
-- ----------------------------
INSERT INTO `uc_account_relation` VALUES (1, 1, 2, '2023-01-11 22:17:30');
INSERT INTO `uc_account_relation` VALUES (2, 2, 3, '2023-01-11 22:17:40');
INSERT INTO `uc_account_relation` VALUES (3, 3, 1, '2023-01-11 22:17:47');

SET FOREIGN_KEY_CHECKS = 1;
