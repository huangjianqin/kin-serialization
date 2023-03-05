package org.kin.kinbuffer;

import org.kin.kinbuffer.runtime.Generic;
import org.kin.kinbuffer.runtime.GenericType;

import java.io.Serializable;

/**
 * @author huangjianqin
 * @date 2023/3/5
 */
@Generic(types = {@GenericType(code = 1, type = GenericImpl1.class), @GenericType(code = 2, type = GenericImpl2.class)})
public class GenericParent implements Serializable {
    private static final long serialVersionUID = -949200482669996795L;

    @Override
    public String toString() {
        return "GenericParent{}";
    }
}
