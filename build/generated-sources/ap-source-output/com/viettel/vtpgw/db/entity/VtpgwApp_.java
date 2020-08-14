package com.viettel.vtpgw.db.entity;

import com.viettel.vtpgw.db.entity.VtpgwPermission;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2020-08-14T09:24:11")
@StaticMetamodel(VtpgwApp.class)
public class VtpgwApp_ { 

    public static volatile SingularAttribute<VtpgwApp, String> updatedBy;
    public static volatile SingularAttribute<VtpgwApp, String> createdBy;
    public static volatile SingularAttribute<VtpgwApp, Date> created;
    public static volatile SingularAttribute<VtpgwApp, String> appId;
    public static volatile SingularAttribute<VtpgwApp, String> contact;
    public static volatile SingularAttribute<VtpgwApp, Long> id;
    public static volatile SingularAttribute<VtpgwApp, String> applicationId;
    public static volatile SingularAttribute<VtpgwApp, Date> updated;
    public static volatile SingularAttribute<VtpgwApp, Integer> status;
    public static volatile SingularAttribute<VtpgwApp, String> token;
    public static volatile CollectionAttribute<VtpgwApp, VtpgwPermission> vtpgwPermissionCollection;

}