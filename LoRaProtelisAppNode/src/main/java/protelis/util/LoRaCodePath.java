package protelis.util;

import org.protelis.vm.util.CodePath;

import gnu.trove.list.TByteList;

public class LoRaCodePath extends CodePath {

    private static final long serialVersionUID = -416432440413815642L;
    private TByteList stack;
    
    public LoRaCodePath(TByteList stack) {
        super(stack);
        this.stack = stack;
    }

    public TByteList getStack() {
        return this.stack;
    }

}
