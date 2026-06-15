import { isIndexedDBAvailable } from '@/api/browser'
import ImMemoryDB from './memoryDb'
import ImIndexedDB from './indexedDb'

// 单例
let dbInstance = null

export async function initDB() {
    if (dbInstance) {
        return dbInstance;
    }
    const canUseIDB = await isIndexedDBAvailable()
    dbInstance = canUseIDB ? new ImIndexedDB() : new ImMemoryDB();
    console.log("本地数据库:", canUseIDB ? "IndexedDB" : "MemoryDB")
    return dbInstance
}

// 同步获取（必须保证 initDB 已经执行过）
export function getDB() {
    if (!dbInstance) {
        throw new Error('DB 还未初始化，请先调用 await initDB()')
    }
    return dbInstance
}

export default {
    initDB,
    getDB
}