package com.anda.smartlock.protocol;

import org.apache.commons.codec.binary.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;


/**
 * 蓝牙锁自定义协议结构解析
 */
public class Lock {

    // magic code
    private static final char MAGIC = 0xFECF;
    // 包头长度
    private static final short HEAD_LENGTH = 12;
    private static Charset CHARSET = Charset.forName("UTF-8");
    public Head head;
    public String body;

    /**
     * 构造类型
     *
     * @param cmdId
     *            命令
     * @param respText
     *            包体内容
     * @param seq
     *            序列号 响应包同传入参数值；push包为0
     */
    public static Lock build(CmdId cmdId, String respText, short seq) {
        Lock lock = new Lock();
        lock.body = respText;
        lock.head = new Head();

        byte[] b = respText == null ? new byte[0] : respText.getBytes(CHARSET);

        lock.head.magic = MAGIC;
        lock.head.version = 1;
        lock.head.length = (short) (HEAD_LENGTH + b.length);
        lock.head.cmdId = cmdId.value();
        lock.head.seq = seq;
        lock.head.errorCode = 0;
        return lock;
    }

    /**
     * 构造类型
     *
     * @param cmdId
     *            命令
     * @param respText
     *            包体内容
     * @param seq
     *            序列号 响应包同传入参数值；push包为0
     */
    public static Lock build(CmdId cmdId, String respText, short seq, short errorCode) {
        Lock lock = new Lock();
        lock.body = respText;
        lock.head = new Head();

        byte[] b = respText == null ? new byte[0] : respText.getBytes(CHARSET);

        lock.head.magic = MAGIC;
        lock.head.version = 1;
        lock.head.length = (short) (HEAD_LENGTH + b.length);
        lock.head.cmdId = cmdId.value();
        lock.head.seq = seq;
        lock.head.errorCode = errorCode;
        return lock;
    }

    /**
     * 二进制转对象
     */
    public static Lock parse(byte[] reqBytes) {
        ByteBuffer buf = ByteBuffer.wrap(reqBytes);
        char magic = buf.getChar();
        // magic校验
        if (magic != MAGIC) {
            System.err.println("magic not valid " + magic);
        }

        Head h = new Head();
        h.magic = MAGIC;
        h.version = buf.getShort();
        h.length = buf.getShort();
        h.cmdId = buf.getShort();
        h.seq = buf.getShort();
        h.errorCode = buf.getShort();

        int bodyLen = h.length - HEAD_LENGTH;
        byte[] bodyBytes = new byte[bodyLen];
        buf.get(bodyBytes);
        String b = new String(bodyBytes, CHARSET);

        Lock lock = new Lock();
        lock.head = h;
        lock.body = b;
        return lock;
    }

    static char[] bytesToChars(byte[] bytes) {
        char[] cs = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            cs[i] = (char) bytes[i];
        }
        return cs;
    }

    static byte[] charsToBytes(char[] chars) {
        byte[] bs = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bs[i] = (byte) chars[i];
        }
        return bs;
    }

    protected static String bytesToHex(byte[] b) {
        char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < b.length; j++) {
            buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
            buf.append(hexDigit[b[j] & 0x0f]);
        }
        return buf.toString();
    }

    public static void main(String[] args) {
        Lock light = Lock.build(CmdId.pushToServ,
                "Hello,WeChat!", (short) 0);
        System.out.println(light);
        System.out.println(Base64.encodeBase64String(light.toBytes()));

    }

    /**
     * 转为二进制
     */
    public byte[] toBytes() {
        byte[] b = body == null ? new byte[0] : body.getBytes(CHARSET);

        ByteBuffer buf = ByteBuffer.allocate(head.length);
        buf.putChar(head.magic);
        buf.putShort(head.version);
        buf.putShort(head.length);
        buf.putShort(head.cmdId);
        buf.putShort(head.seq);
        buf.putShort(head.errorCode);
        buf.put(b);

        buf.flip();
        return buf.array();
    }

    @Override
    public String toString() {
        return "Lock [body=" + body + ", head=" + head + "]";
    }

    /**
     * 命令
     */
    public enum CmdId {
        //发送数据（指纹地址）到服务器。
        sendDataToServ(0x01),

        //发送数据到服务器的回包。
        sendDataToServACK(0x1001),

        //发送数据（指纹地址）到设备。
        sendDataToLock(0x02),

        //发送数据（指纹地址）到设备的回包。
        sendDataToLockACK(0x1002),

        //发送通知指纹模块进入录指纹模式。
        sendAddNotificationToLock(0x03),

        //设备返回指纹模块开启的状态，errorcode:0x0000表示成功开始，0x0002表示开启指纹录取模式失败。
        sendAddNotificationToLockACK(0x1003),

        //发送通知指纹模块进入删除模式。
        sendDelNotificationToLock(0x04),

        //设备返回指纹模块开启的状态，errorcode:0x0000表示成功开始，0x0006表示开启指纹删除模式失败。
        sendDelNotificationToLockACK(0x1004),

        //设备通知服务器消息，不需要服务器回复
        pushToServ(0x2001),

        //服务器通知设备，不需要设备回复
        pushToLock(0x2002);

        private short value;

        CmdId(int v) {
            this.value = (short) v;
        }

        public short value() {
            return value;
        }
    }

    /**
     * <pre>
     * 	包 由包头+包体组成，包头如：
     * 	struct LockDemoHead
     *    {
     * 	    unsigned char  m_magicCode[2];
     * 	    unsigned short m_version;
     * 	    unsigned short m_totalLength;
     * 	    unsigned short m_cmdId;
     * 	    unsigned short m_seq;
     * 	    unsigned short m_errorCode;
     *    };
     *  包体为字符串 utf-8编码
     * </pre>
     */

    public static class Head {
        public char magic;// 固定为 0xFECF
        public short version;
        public short length;// 包头+包体总长度
        public short cmdId;// 命令字
        public short seq;// 序列号 resp时为参数 push时为0
        public short errorCode;// 错误代码 0表示成功

        @Override
        public String toString() {
            return "Head [magic=" + magic + ", version=" + version
                    + ", length=" + length + ", cmdId=" + cmdId + ", seq="
                    + seq + ", errorCode=" + errorCode + "]";
        }
    }
}
