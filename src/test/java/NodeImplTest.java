import com.bmc.bagdb.model.node.INode;
import com.bmc.bagdb.model.nodeset.INodeSet;
import com.bmc.bagdb.model.relationship.IRelationship;
import com.bmc.bagdb.model.role.IRole;
import com.bmc.bagdb.repository.BagDB;
import com.bmc.bagdb.repository.Partition;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.ITestResult;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Listeners({utils.Listeners.MyListener.class})
public class NodeImplTest {
    static BagDB llMainJavaAdaptor = BagDB.getInstance();
    private static Map<String, Object> state = new HashMap();
    private static Partition partitionDefault;
    private static Partition partitionLogs;
    private static INode sourceNode;
    private static INode destinationNode;
    private static IRelationship relationship;
    private static String kind;
    static INodeSet createNodeSet;
    private static Optional<Partition> partition1;
    private static Optional<Partition> partition2;
    private static long timeInMilli;
    private static String partitionName1,partitionName2;
    ExtentTest logger;
    ITestResult result;

    /**
     * Created one node on default partition and one node on Logs partition
     * Created relation between these two nodes as part of prerequisite.
     */
    @BeforeClass
    public static void setUp(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        Date date = new Date();
        timeInMilli = date.getTime();
        partitionName1 = "testPartition1" + timeInMilli;
        partitionName2 = "testPartition2" + timeInMilli;

        partition1 = llMainJavaAdaptor.createPartition(partitionName1, 0);
        partition2 = llMainJavaAdaptor.createPartition(partitionName2, 0);

        /*Common Node and relationship created for UT's */
        kind = "test";
        state.put("IP", "localhost");
        state.put("Host", 9090);
        state.put("temp", "justForupdate");

        // Creating New node on Default partition
        partitionDefault = llMainJavaAdaptor.getStoreByName(partitionName1).get();
        state.put("node", "SourceNode");
        sourceNode = partitionDefault.createNode(kind, state); // Node 1 (source) created

        // Creating New node on Logs partition
        partitionLogs = llMainJavaAdaptor.getStoreByName(partitionName2).get();
        state.put("node", "DestinationNode"); //
        destinationNode = partitionLogs.createNode("Loggertest", state);//  Node 2 (destination) created

        // Creating Relationship between sourceNode and destination node
        state.put("node", "relationshipNode");
        relationship = sourceNode.createRelationship("fromRole", kind, "toRole", destinationNode, state);
    }

    /**
     * Verifies the step-in of node in relationship
     */
    @Test
    public void stepIn(Method method) {
        /* Command Nodes and Relationship created in setUp method  and destroyed in tearDown Method*/
        //half traverse from source node to relationship node.
            List<String> relsOfNode = sourceNode.getRelsOfNode();
            String[] split = relsOfNode.get(0).split(":");
            List<IRelationship> stepIn = sourceNode.stepIn(split[0], split[1], 0, 1);
            List<IRole> rolesOfRelationship = stepIn.get(0).getRolesOfRelationship();
            List<IRole> rolesOfRelationship2 = relationship.getRolesOfRelationship();
            Assert.assertEquals(rolesOfRelationship.get(0).getRole(), rolesOfRelationship2.get(0).getRole());
            Assert.assertTrue(false);

    }

    @Test
    public void stepIn1(Method method) {
            Assert.assertFalse(false);

    }

    /**
     * Destroy partition created for common operation
     */
    @AfterClass
    public static void tearDown() throws IllegalArgumentException, IOException {
        partition1.get().destroyPartition();
        partition2.get().destroyPartition();
    }

}
