package com.bmc.bagdb;

import com.bmc.bagdb.Authentication;
import com.bmc.bagdb.ModelCORBA.DataStore.LowLevel.MainValueDefaultFactory;
import com.bmc.bagdb.ModelCORBA.DataStore.LowLevel.MainValueHelper;
import com.bmc.bagdb.ModelCORBA.DataStore.*;
import com.bmc.bagdb.SecurityCORBA.PasswordAuthenticator;
import com.bmc.bagdb.SecurityCORBA.PasswordAuthenticatorHelper;
import com.bmc.bagdb.factory.InitConstants;
import org.omg.CORBA_2_3.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;


public class ResolverFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.bmc.bagdb.factory.ResolverFactory.class);
    public static String BAGDB_CLUSTER = "BAGDB_CLUSTER";
    public static String DEFAULT_BAGDB_HOST_PORT = "clm-pun-u487wd.bmc.com:25020";
    private static ORB orb;
    private String bagDbHostPort = "";

    public static com.bmc.bagdb.factory.ResolverFactory getInstance() {
        return com.bmc.bagdb.factory.ResolverFactory.INSTANCE_HOLDER._instance;
    }

    private String getBagDBHostName() {
        bagDbHostPort = System.getProperty(BAGDB_CLUSTER);
        if (null == bagDbHostPort || bagDbHostPort.trim().isEmpty()) {
            bagDbHostPort= DEFAULT_BAGDB_HOST_PORT;
        }
        LOGGER.info("Will connect to BagDb host ", bagDbHostPort);
        return bagDbHostPort;
    }

    public org.omg.CORBA.ORB getOrb(String[] args) {
        if (Objects.isNull(orb)) {
            // Set up properties to use JacORB and register our.
            // authentication interceptor.
            Properties props = new Properties();

            props.setProperty(InitConstants.orbClassKey,
                    InitConstants.jacorbOrb);

            props.setProperty(InitConstants.corbaSingletonKey,
                    InitConstants.jacorbOrbSingleton);

            props.setProperty(InitConstants.portableInterceptorKey,
                    InitConstants.bagdbAuthentication);

            orb = (ORB) ORB.init(args, props);
            this.registerFactories();
        }
        return (org.omg.CORBA.ORB) orb;
    }

    /**
     * This method registers the factory instances for corresponding
     * implementations. Ideally it should have been done by IDLJ
     * compiler while auto generating the code equivalent to idl
     * files provided.
     */
    private void registerFactories() {
        ((ORB) orb).register_value_factory(MainValueHelper.id(), new MainValueDefaultFactory());
        ((ORB) orb).register_value_factory(PartitionValHelper.id(), new PartitionValDefaultFactory());
        ((ORB) orb).register_value_factory(NodeValHelper.id(), new NodeValDefaultFactory());
        ((ORB) orb).register_value_factory(RelationshipValHelper.id(), new RelationshipValDefaultFactory());
        ((ORB) orb).register_value_factory(RoleHelper.id(), new RoleDefaultFactory());
    }



    public Resolver getResolver() {
        if (Objects.isNull(orb)) {
            getOrb(null);
        }
        // Connect to the CORBA naming service. (Assumes the
        // container is on localhost and has been run with
        // --hostname localhost )
        org.omg.CORBA.Object obj;
        obj = orb.string_to_object("corbaloc::"+getBagDBHostName()+"/Resolver");
        Resolver resolver = ResolverHelper.narrow(obj);

        // getting hold of PasswordAuthenticator
        PasswordAuthenticator passwordAuthenticator = resolver.getPasswordAuthenticator();
        //Setting credentials
        Authentication.setInfo(passwordAuthenticator.authenticate("bagdb", "bagdb"));
        return resolver;
    }

    public static class INSTANCE_HOLDER {
        public static com.bmc.bagdb.factory.ResolverFactory _instance = new com.bmc.bagdb.factory.ResolverFactory();

    }

}
