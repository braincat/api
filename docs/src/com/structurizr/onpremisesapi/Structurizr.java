package com.structurizr.onpremisesapi;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.componentfinder.*;
import com.structurizr.documentation.Documentation;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.Type;
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
        Documentation documentation = workspace.getDocumentation();

        Person softwareDeveloper = model.addPerson(Location.Internal, "Software Developer", "A software developer.");
        SoftwareSystem structurizr = model.addSoftwareSystem(Location.External, "Structurizr", "Visualise, document and explore your software architecture.");
        SoftwareSystem structurizrApi = model.addSoftwareSystem(Location.Internal, "Structurizr API", "A simple implementation of the Structurizr API, which is designed to be run on-premises to support Structurizr's on-premises API feature.");

        softwareDeveloper.uses(structurizr, "Uses");
        structurizr.uses(structurizrApi, "Gets and puts workspace data using");

        Container apiServer = structurizrApi.addContainer("API Application", "A simple implementation of the Structurizr API, which is designed to be run on-premises to support Structurizr's on-premises API feature.", "Java EE web application");
        Container fileSystem = structurizrApi.addContainer("File System", "Stores workspace data.", "");

        structurizr.uses(apiServer, "Gets and puts workspaces using");
        apiServer.uses(fileSystem, "Stores information on");

        File sourceRoot = new File("src");

        ComponentFinder componentFinder = new ComponentFinder(
                apiServer,
                "com.structurizr.onpremisesapi",
                new TypeBasedComponentFinderStrategy(
                        new NameSuffixTypeMatcher("Servlet", "", "Java Servlet")
                ),
                new StructurizrAnnotationsComponentFinderStrategy(
                        new FirstImplementationOfInterfaceSupportingTypesStrategy(),
                        new ReferencedTypesSupportingTypesStrategy()
                ),
                new SourceCodeComponentFinderStrategy(sourceRoot, 150));
        componentFinder.findComponents();

        // link the architecture model with the code
        for (Component component : apiServer.getComponents()) {
            String sourcePath = component.getSourcePath();
            if (sourcePath != null) {
                component.setSourcePath(sourcePath.replace(
                        sourceRoot.getCanonicalPath(),
                        "https://github.com/structurizr/api/tree/master/src"));
            }
        }

        structurizr.uses(apiServer.getComponentWithName("ApiServlet"), "Gets and puts workspaces using", "JSON/HTTPS");

        SystemContextView contextView = views.createSystemContextView(structurizrApi, "Context", "The system context for the Structurizr API.");
        contextView.addAllElements();

        ContainerView containerView = views.createContainerView(structurizrApi, "Containers", "The containers that make up the Structurizr API.");
        containerView.addAllElements();

        ComponentView componentView = views.createComponentView(apiServer, "Components", "The components within the API Server.");
        componentView.addAllElements();

        structurizr.addTags(ON_THE_CLOUD);
        softwareDeveloper.addTags(BEHIND_FIREWALL);
        structurizrApi.addTags(BEHIND_FIREWALL);
        apiServer.addTags(BEHIND_FIREWALL);
        fileSystem.addTags(BEHIND_FIREWALL);

        apiServer.getComponents().stream().forEach(c -> c.addTags(BEHIND_FIREWALL));

        styles.addElementStyle(Tags.ELEMENT).color("#ffffff").shape(Shape.RoundedBox);
        styles.addElementStyle(Tags.SOFTWARE_SYSTEM);
        styles.addElementStyle(Tags.PERSON).shape(Shape.Person);
        styles.addElementStyle(Tags.CONTAINER);
        styles.addElementStyle(Tags.COMPONENT);

        styles.addElementStyle(ON_THE_CLOUD).background("#85bbf0");
        styles.addElementStyle(BEHIND_FIREWALL).background("#1168bd");

        File documentationRoot = new File(".");
        documentation.addImages(documentationRoot);
        documentation.add(structurizrApi, Type.Context, Format.Markdown, new File(documentationRoot, "context.md"));
        documentation.add(structurizrApi, Type.Data, Format.Markdown, new File(documentationRoot, "data.md"));
        documentation.add(structurizrApi, Type.Containers, Format.Markdown, new File(documentationRoot, "containers.md"));
        documentation.add(apiServer, Format.Markdown, new File(documentationRoot, "components.md"));
        documentation.add(structurizrApi, Type.DevelopmentEnvironment, Format.Markdown, new File(documentationRoot, "development-environment.md"));
        documentation.add(structurizrApi, Type.Deployment, Format.Markdown, new File(documentationRoot, "deployment.md"));
        documentation.add(structurizrApi, Type.Usage, Format.Markdown, new File(documentationRoot, "usage.md"));

        StructurizrClient client = new StructurizrClient(System.getProperty("structurizr.api.key"), System.getProperty("structurizr.api.secret"));
        client.mergeWorkspace(18571, workspace);
//        StructurizrClient client = new StructurizrClient("https://localhost:9443", "6b274e26-83cf-4a96-a975-9017292f6584", "f676268e-9ef4-4f17-aada-a3ca64a4ba87");
//        client.mergeWorkspace(47, workspace);
    }

}
