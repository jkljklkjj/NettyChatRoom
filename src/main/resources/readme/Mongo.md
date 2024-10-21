# MonggoDB的接入

形式如下
int id; 表明接入mysql的id
int userid； monggodb自动生成的id
List`<ObjectId>` friends; 朋友的id列表
List`<ObjectId>` groups; 群聊的id列表
List`<ObjectId>` friendChat; 以每个好友的id为键，记录对应的聊天记录，最多每个好友记录一百条
List`<ObjectId>` groupChat; 以每个群组id为键，记录对应的聊天记录，每个群最多记录五百条
