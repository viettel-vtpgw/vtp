package com.viettel.vtpgw.db.entity;

import com.viettel.vtpgw.db.entity.VtpgwApp;
import java.math.BigInteger;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2020-08-14T09:24:10")
@StaticMetamodel(VtpgwPermission.class)
public class VtpgwPermission_ { 

    public static volatile SingularAttribute<VtpgwPermission, Integer> noContent;
    public static volatile SingularAttribute<VtpgwPermission, String> permissionId;
    public static volatile SingularAttribute<VtpgwPermission, BigInteger> period;
    public static volatile SingularAttribute<VtpgwPermission, String> updatedBy;
    public static volatile SingularAttribute<VtpgwPermission, Integer> debug;
    public static volatile SingularAttribute<VtpgwPermission, Date> created;
    public static volatile SingularAttribute<VtpgwPermission, String> methods;
    public static volatile SingularAttribute<VtpgwPermission, Integer> sandBox;
    public static volatile SingularAttribute<VtpgwPermission, String> ips;
    public static volatile SingularAttribute<VtpgwPermission, BigInteger> capacity;
    public static volatile SingularAttribute<VtpgwPermission, String> createdBy;
    public static volatile SingularAttribute<VtpgwPermission, VtpgwApp> appId;
    public static volatile SingularAttribute<VtpgwPermission, Long> id;
    public static volatile SingularAttribute<VtpgwPermission, String> serviceId;
    public static volatile SingularAttribute<VtpgwPermission, Date> updated;
    public static volatile SingularAttribute<VtpgwPermission, BigInteger> activated;

}