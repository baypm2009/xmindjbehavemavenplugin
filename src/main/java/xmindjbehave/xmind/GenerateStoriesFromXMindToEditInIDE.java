package xmindjbehave.xmind;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.xmind.core.INotes;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ITopic;
import xmindjbehave.jbehave.JBehaveTextProcessor;
import xmindjbehave.jbehave.concatenate.Concatenator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Ilya Evlampiev on 06.03.15.
 */
@Mojo(name = "generateStoriesFromXMindToEditInIDE")
public class GenerateStoriesFromXMindToEditInIDE extends AbstractXMindToSpecsMojo {

    //needed for testing and debugging
    @Deprecated
    public static void main(String[] args) {
        GenerateStoriesFromXMindToEditInIDE gen = new GenerateStoriesFromXMindToEditInIDE();
        try {
            gen.outputDirectory = new File("");
            gen.xmindpath = "C:\\pegas\\regression.xmind";

            gen.execute();
        } catch (MojoExecutionException e) {
            e.printStackTrace();
        } catch (MojoFailureException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void iterateOverTopic(ITopic itop, String offset, String folderBase, String textFromTheParentNode) throws IOException {

        System.out.println(offset);
        //creating the folder to include the spec extracted from the topic note
        boolean folderCreated = (new File(folderBase)).mkdirs();
        //obtaining text from the node if it exists
        String valueforTheCurrentTopicNote = "";
        IPlainNotesContent plainContent = (IPlainNotesContent) itop.getNotes().getContent(INotes.PLAIN);
        if (plainContent != null) {
            valueforTheCurrentTopicNote = plainContent.getTextContent();
            Concatenator c = new Concatenator(textFromTheParentNode, valueforTheCurrentTopicNote);
            valueforTheCurrentTopicNote = c.getResult();
        }
        //if there are children we go deeper
        if (itop.getAllChildren().size() > 0) {
            for (ITopic child : itop.getAllChildren()) {
                iterateOverTopic(child, offset + " ", folderBase + "\\" + itop.getTitleText().replace(",","").replace("\r", "").replace("\n", "").replace("\"","").replace("\'", "").replace(">", "").replace("<","").replace("*","").replace(":","").replace(";","").replace("/","").trim(), valueforTheCurrentTopicNote);
            }
        }
        //else we are creating spec file
        else {
            //if only it is marked with the correct flag
            if (this.topicOrParentHaveMarker(itop, "symbol-exclam")) {
                if(valueforTheCurrentTopicNote.equals("")){valueforTheCurrentTopicNote="Narrative:\n" +
                        "New test for "+itop.getTitleText();}
                System.out.println("\r\n\r\nScenario: "
                        + itop.getTitleText()
                        + "\r\n\r\n"
                        + valueforTheCurrentTopicNote
                        + "\r\n\r\n");
                File newStoryCreated = new File(folderBase + "\\" + itop.getTitleText().replace(",","").replace("\r","").replace("\n","").replace("\"","").replace("\'","").replace(">","").replace("<","").replace("*","").replace(":","").replace(";","").replace("/","").trim() + ".story");
                BufferedWriter writer = new BufferedWriter(new FileWriter(newStoryCreated));
                JBehaveTextProcessor jbehaveTextParser = new JBehaveTextProcessor(valueforTheCurrentTopicNote.trim(),hostPort);
                writer.write(jbehaveTextParser.run());
                writer.close();
            }

        }

    }
}
