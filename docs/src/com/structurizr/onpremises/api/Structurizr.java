package com.structurizr.onpremises.api;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.componentfinder.*;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.StructurizrDocumentation;
import com.structurizr.model.*;
import com.structurizr.view.*;

import java.io.File;

/**
 * Creates software architecture diagrams and documentation using Structurizr.
 *
 * The live version is available at https://structurizr.com/public/18571/documentation
 */
public class Structurizr {

    private static final String BEHIND_FIREWALL = "Behind the firewall";
    private static final String ON_THE_CLOUD = "On the cloud";

    public static void main(String[] args) throws Exception {
        Workspace workspace = new Workspace("Structurizr API", "The on-premises version of the Structurizr API.");
        Model model = workspace.getModel();
        model.setEnterprise(new Enterprise("Your organization"));
        ViewSet views = workspace.getViews();
        Styles styles = workspace.getViews().getConfiguration().getStyles();
        StructurizrDocumentation documentation = new StructurizrDocumentation(model);
        workspace.setDocumentation(documentation);

        // software systems and people
        Person softwareDeveloper = model.addPerson(Location.Internal, "Software Developer", "A software developer.");
        SoftwareSystem structurizr = model.addSoftwareSystem(Location.External, "Structurizr", "Visualise, document and explore your software architecture.");
        SoftwareSystem structurizrApi = model.addSoftwareSystem(Location.Internal, "Structurizr API", "A simple implementation of the Structurizr API, which is designed to be run on-premises to support Structurizr's on-premises API feature.");

        softwareDeveloper.uses(structurizr, "Uses");
        structurizr.uses(structurizrApi, "Gets and puts workspace data using");

        // containers
        Container apiApplication = structurizrApi.addContainer("API Application", "A simple implementation of the Structurizr API, which is designed to be run on-premises to support Structurizr's on-premises API feature.", "Java EE web application");
        Container fileSystem = structurizrApi.addContainer("File System", "Stores workspace data.", "Local or network file system");

        structurizr.uses(apiApplication, "Gets and puts workspaces using", "JSON/HTTPS");
        apiApplication.uses(fileSystem, "Stores information on");

        // components
        File sourceRoot = new File("../src");

        ComponentFinder componentFinder = new ComponentFinder(
                apiApplication,
                "com.structurizr.onpremises",
                new StructurizrAnnotationsComponentFinderStrategy(
                        new FirstImplementationOfInterfaceSupportingTypesStrategy(),
                        new ReferencedTypesSupportingTypesStrategy()
                ),
                new SourceCodeComponentFinderStrategy(sourceRoot, 150));
        componentFinder.findComponents();

        structurizr.uses(apiApplication.getComponentWithName("ApiServlet"), "Gets and puts workspaces using", "JSON/HTTPS");

        // link the architecture model with the code
        for (Component component : apiApplication.getComponents()) {
            for (CodeElement codeElement : component.getCode()) {
                String sourcePath = codeElement.getUrl();
                if (sourcePath != null) {
                    codeElement.setUrl(sourcePath.replace(
                            sourceRoot.getCanonicalFile().toURI().toString(),
                            "https://github.com/structurizr/api/tree/master/src/"));
                }
            }
        }

        // views
        SystemContextView contextView = views.createSystemContextView(structurizrApi, "Context", "The system context for the Structurizr API.");
        contextView.addAllElements();

        ContainerView containerView = views.createContainerView(structurizrApi, "Containers", "The containers that make up the Structurizr API.");
        containerView.addAllElements();

        ComponentView componentView = views.createComponentView(apiApplication, "Components", "The components within the API Application.");
        componentView.addAllElements();

        // tags
        structurizr.addTags(ON_THE_CLOUD);
        softwareDeveloper.addTags(BEHIND_FIREWALL);
        structurizrApi.addTags(BEHIND_FIREWALL);
        apiApplication.addTags(BEHIND_FIREWALL);
        fileSystem.addTags(BEHIND_FIREWALL);
        apiApplication.getComponents().stream().forEach(c -> c.addTags(BEHIND_FIREWALL));

        // styles
        styles.addElementStyle(Tags.ELEMENT).color("#ffffff").shape(Shape.RoundedBox);
        styles.addElementStyle(Tags.SOFTWARE_SYSTEM);
        styles.addElementStyle(Tags.PERSON).shape(Shape.Person);
        styles.addElementStyle(Tags.CONTAINER);
        styles.addElementStyle(Tags.COMPONENT).fontSize(20);
        styles.addElementStyle(ON_THE_CLOUD).background("#85bbf0");
        styles.addElementStyle(BEHIND_FIREWALL).background("#1168bd");

        // documentation
        File documentationRoot = new File(".");
        documentation.addImages(documentationRoot);
        documentation.addContextSection(structurizrApi, Format.Markdown, new File(documentationRoot, "context.md"));
        documentation.addContainersSection(structurizrApi, Format.Markdown, new File(documentationRoot, "containers.md"));
        documentation.addComponentsSection(apiApplication, Format.Markdown, new File(documentationRoot, "components.md"));
        documentation.addDataSection(structurizrApi, Format.Markdown, new File(documentationRoot, "data.md"));
        documentation.addDeploymentSection(structurizrApi, Format.Markdown, new File(documentationRoot, "deployment.md"));
        documentation.addCustomSection(structurizrApi, "Usage", 4, Format.Markdown, new File(documentationRoot, "usage.md"));
        documentation.addDevelopmentEnvironmentSection(structurizrApi, Format.Markdown, new File(documentationRoot, "development-environment.md"));

        // upload to Structurizr
        String apiKey = System.getenv("STRUCTURIZR_API_KEY");
        String apiSecret = System.getenv("STRUCTURIZR_API_SECRET");
        if (apiKey != null && apiSecret != null) {
            StructurizrClient client = new StructurizrClient(apiKey, apiSecret);
            client.putWorkspace(18571, workspace);
        } else {
            System.out.println("API key/secret not specified ... skipping upload of software architecture model to Structurizr.");
        }
    }

}
