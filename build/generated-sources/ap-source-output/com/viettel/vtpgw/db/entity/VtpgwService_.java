package com.viettel.vtpgw.db.entity;

import com.viettel.vtpgw.db.entity.VtpgwNodes;
import java.math.BigInteger;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2020-08-14T09:24:11")
@StaticMetamodel(VtpgwService.class)
public class VtpgwService_ { 

    public static volatile SingularAttribute<VtpgwService, BigInteger> period;
    public static volatile SingularAttribute<VtpgwService, String> updatedBy;
    public static volatile SingularAttribute<VtpgwService, Date> created;
    public static volatile SingularAttribute<VtpgwService, String> module;
    public static volatile SingularAttribute<VtpgwService, String> description;
    public static volatile SingularAttribute<VtpgwService, BigInteger> capacity;
    public static volatile SingularAttribute<VtpgwService, Integer> reportInterval;
    public static volatile SingularAttribute<VtpgwService, BigInteger> standardDuration;
    public static volatile SingularAttribute<VtpgwService, String> createdBy;
    public static volatile SingularAttribute<VtpgwService, String> contact;
    public static volatile SingularAttribute<VtpgwService, BigInteger> idleTimeout;
    public static volatile SingularAttribute<VtpgwService, BigInteger> connectTimeout;
    public static volatile SingularAttribute<VtpgwService, String> name;
    public static volatile SingularAttribute<VtpgwService, String> sandboxEndpoint;
    public static volatile SingularAttribute<VtpgwService, Long> id;
    public static volatile SingularAttribute<VtpgwService, String> serviceId;
    public static volatile SingularAttribute<VtpgwService, Date> updated;
    public static volatile CollectionAttribute<VtpgwService, VtpgwNodes> vtpgwNodesCollection;
    public static volatile SingularAttribute<VtpgwService, Integer> status;

}