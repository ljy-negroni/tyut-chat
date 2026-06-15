
class BaseDB {

  constructor() {
    this.userId = null;
  }

  open(userId) {
    this.userId = userId
  }

  close() {

  }

  buildConversationKey(type, targetId) {
    return type + '-' + targetId;
  }
}

export default BaseDB;