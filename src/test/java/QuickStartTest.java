import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.bmc.bagdb.factory.MainFactory;
import com.bmc.bagdb.factory.ResolverFactory;
import com.bmc.bagdb.repository.BagDB;
import com.bmc.bagdb.repository.Partition;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.bmc.bagdb.model.relationship.IRelationship;
import com.bmc.bagdb.ModelCORBA.DataStore.NoSuchPartition;
import com.bmc.bagdb.model.node.INode;


public class QuickStartTest {
    BagDB bagdb;
    INode sourceNode, destinationNode;
    IRelationship relationship;
    Partition testPartition;
    private static long timeInMilli;
    private static String partitionName1;

    @BeforeClass
    public void setup() throws IOException {

        Properties properties = new Properties();
        Properties prop = readPropertiesFile("src//test//resources//application.properties");
//       System.out.println(prop.getProperty("BAGDB_CLUSTER"));
        String bagDBCluster = prop.getProperty(ResolverFactory.BAGDB_CLUSTER);
        System.setProperty(ResolverFactory.BAGDB_CLUSTER, bagDBCluster);
        bagdb = BagDB.getInstance();

    }

    @Test
    public void quickStartDemoTest(Method method) throws NoSuchPartition, IOException {
        //set property to point to desired bagdb host
        //Initialize   BagDB

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        Date date = new Date();
        timeInMilli = date.getTime();
        partitionName1 = "testPartition1" + timeInMilli;

        Optional<Partition> partitionOptional = bagdb.createPartition(partitionName1, 0);
        Assert.assertEquals(partitionName1, partitionOptional.get().getPartitionName());

        testPartition = partitionOptional.get();
        String kind = "VM";

        // Creating  node on startUpTestpartition, it required kind and state.
        // kind = type of node.
        // state = attribute of node.
        Map<String, Object> sourceNodeState = getstate();
        sourceNodeState.put("class", "ComputerSystem");
        sourceNode = testPartition.createNode(kind, sourceNodeState); // Node 1 (source) created
        Assert.assertEquals("ComputerSystem", sourceNode.getState().get("class"));

        // find nodes. Required param kind, state, flag, records
        // param : kind = node kind
        // param : state = attributes of node
        // param : flag = filter the search as per flag.
        // param : records = max number of nodes to be found
        List<INode> findNodes = testPartition.findNodes(kind, sourceNodeState, 0, 1);
        Assert.assertEquals(sourceNode.get("class"), findNodes.get(0).get("class"));

        Map<String, Object> destinationNodeState = getstate();
        destinationNodeState.put("class", "RemoteSystem");
        destinationNode = testPartition.createNode(kind, destinationNodeState); // Node 2 (destination) created
        Assert.assertEquals("RemoteSystem", destinationNode.getState().get("class"));

        // Creating Relationship between sourceNode and destination node
        // it required role of source node, relationship node kind, role of destination node, destination node and state.
        Map<String, Object> relationshipNodeState = getstate();
        relationshipNodeState.put("class", "ConnectionBridge");
        IRelationship relationship = sourceNode.createRelationship("provider", kind, "consumer", destinationNode, relationshipNodeState);
        Assert.assertEquals("ConnectionBridge", relationship.getState().get("class"));

        // traverse from source node to destination node using expression;
        // expression = role of source node:relationship node kind:role of destination node:destination node kind.
        // param : nodeId = source node_id
        // param : from_role = role of source node
        // param : rel_kind = relationship node kind
        // param : to_role = role of destination node
        // param : node_kind = destination node kind
        // param : flags = filter the search as per flag.
        // param : records = max number of nodes to be found
        List<INode> traverse = testPartition.traverse(sourceNode.getNodeId(), "provider", kind, "consumer", kind, 0, 1);
//        Assert.assertArrayEquals( destinationNode.getNodeId(), traverse.get(0).getNodeId());






    }

    private Map<String, Object> getstate() {
        Map<String, Object> state = new HashMap<String, Object>();
        state.put("IP", "localhost");
        state.put("Host", 9090);
        return state;
    }

    public static Properties readPropertiesFile(String fileName) throws IOException {
        FileInputStream fis = null;
        Properties prop = null;
        try {
            fis = new FileInputStream(fileName);
            prop = new Properties();
            prop.load(fis);
        } catch(FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            fis.close();
        }
        return prop;
    }

    @AfterClass
    public void tearDown() throws IOException{
        sourceNode.reallyDestroyNodeAndRelationships();
        destinationNode.reallyDestroyNode();
        //destroy partition
        testPartition.destroyPartition();
    }

}
