package com.viettel.vtpgw.db.entity;

import com.viettel.vtpgw.db.entity.VtpgwService;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2020-08-14T09:24:11")
@StaticMetamodel(VtpgwNodes.class)
public class VtpgwNodes_ { 

    public static volatile SingularAttribute<VtpgwNodes, String> updatedBy;
    public static volatile SingularAttribute<VtpgwNodes, String> createdBy;
    public static volatile SingularAttribute<VtpgwNodes, Date> created;
    public static volatile SingularAttribute<VtpgwNodes, Long> id;
    public static volatile SingularAttribute<VtpgwNodes, VtpgwService> serviceId;
    public static volatile SingularAttribute<VtpgwNodes, Date> updated;
    public static volatile SingularAttribute<VtpgwNodes, String> checkUrl;
    public static volatile SingularAttribute<VtpgwNodes, String> url;
    public static volatile SingularAttribute<VtpgwNodes, Integer> status;

}