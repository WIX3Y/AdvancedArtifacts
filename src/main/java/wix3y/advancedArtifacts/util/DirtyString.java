package wix3y.advancedArtifacts.util;

public class DirtyString {
    private String str;
    private boolean dirty;

    public DirtyString(String str, boolean dirty) {
        this.str = str;
        this.dirty = dirty;
    }

    public String getStr() {
        return str;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty){
        this.dirty = dirty;
    }

    public void updateStr(String str) {
        this.str = str;
    }
}