/**
 * @ClassName File
 * @Author ׂ»לה
 * @Date 2023/5/18 16:24
 * @Version 1.8
 **/
public class HtmlFile {
    private long fileLen = 0;
    private StringBuilder fileBuf = new StringBuilder();

    public void setFileLen(long fileLen) {
        this.fileLen = fileLen;
    }

    public void setFileBuf(StringBuilder fileBuf) {
        this.fileBuf = fileBuf;
    }

    public long getFileLen() {
        return fileLen;
    }

    public StringBuilder getFileBuf() {
        return fileBuf;
    }
}
