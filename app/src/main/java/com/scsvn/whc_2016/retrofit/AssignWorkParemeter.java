package com.scsvn.whc_2016.retrofit;

/**
 * Created by buu on 28/01/2016.
 */
public class AssignWorkParemeter {
    private int QHSEID;
    private int Department = 4;
    private String UserName = "";


    public AssignWorkParemeter(int QHSEID, String UserName) {
        this.QHSEID = QHSEID;
        this.UserName = UserName;
    }
}
