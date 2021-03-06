package io.easeci.core.workspace.projects;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.api.projects.dto.AddProjectGroupRequest;
import io.easeci.api.projects.dto.AddProjectRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;

import static io.easeci.core.workspace.LocationUtils.getProjectsStructureFileLocation;
import static io.easeci.core.workspace.projects.ProjectsFile.defaultProjectGroupId;
import static io.easeci.core.workspace.projects.ProjectsFile.defaultProjectId;
import static io.easeci.core.workspace.projects.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

class ProjectManagerTest extends BaseWorkspaceContextTest {

    @BeforeEach
    void setupEach() {
        try {
            Files.deleteIfExists(getProjectsStructureFileLocation());
            // singleton trap!
            // remember in future: if you are using singleton object it will be one instance object per all test class invocation
            // In order to fix this singleton issue remove one and recreate new object
            ProjectManager.refreshFileContext();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Should correctly add pipeline pointer to 'other' project")
    void createPipelinePointer() {
        // prepare required objects
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();

        // execute testing method
        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        // find just saved pipeline
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        PipelinePointer justAddedPointer = firstPipelinePointer(projectsFile);

        assertAll(() -> assertNotNull(pipelinePointerIO),
                  () -> assertNotNull(justAddedPointer),
                  () -> assertNotNull(pipelinePointer),
                  () -> assertEquals(pipelineMeta.getPipelineId(), justAddedPointer.getPipelineId()),
                  () -> assertEquals(pipelineMeta.getCreatedDate(), justAddedPointer.getCreatedDate()),
                  () -> assertEquals(pipelineMeta.getEasefilePath(), justAddedPointer.getEasefilePath()),
                  () -> assertEquals(pipelineMeta.getName(), justAddedPointer.getName()),
                  () -> assertEquals(pipelineMeta.getPipelineFilePath(), justAddedPointer.getPipelineFilePath()),
                  () -> assertEquals(pipelineMeta.getTag(), justAddedPointer.getTag()),
                  () -> assertEquals(pipelineMeta.getProjectId(), justAddedPointer.getProjectId()));
    }

    @Test
    @DisplayName("Should cannot add pipeline with same id twice")
    void createPipelinePointerIdExists() {
        // prepare required objects
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setName("Another test name");

        // execute testing method
        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        assertThrows(PipelineManagementException.class, () -> pipelinePointerIO.createNewPipelinePointer(pipelineMeta));

        // find just saved pipeline
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        int pipelinesAmount = pipelinesAmount(projectsFile);

        assertAll(() -> assertNotNull(pipelinePointer),
                  () -> assertEquals(1, pipelinesAmount));
    }

    @Test
    @DisplayName("Should cannot add pipeline with just existing name to project")
    void createPipelinePointerNameExists() {
        // prepare required objects
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setPipelineId(UUID.randomUUID());

        // execute testing method
        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        assertThrows(PipelineManagementException.class, () -> pipelinePointerIO.createNewPipelinePointer(pipelineMeta));

        // find just saved pipeline
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        int pipelinesAmount = pipelinesAmount(projectsFile);

        assertAll(() -> assertNotNull(projectsFile),
                  () -> assertEquals(1, pipelinesAmount));
    }

    @Test
    @DisplayName("Should remove correctly pipeline pointer if exists")
    void deletePipelinePointerSuccess() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertNotNull(pipelinePointer);

        Long projectId = pipelineMeta.getProjectId();
        Long pipelineId = 0L;

        int pipelinesAmountBeforeRemoval = pipelinesAmount(projectsFile);
        PipelinePointer pipelinePointerDeleted = pipelinePointerIO.deletePipelinePointer(projectId, pipelineId);
        int pipelinesAmountAfterRemoval = pipelinesAmount(projectsFile);

        assertAll(() -> assertNotNull(pipelinePointerDeleted),
                  () -> assertEquals(1, pipelinesAmountBeforeRemoval),
                  () -> assertEquals(0, pipelinesAmountAfterRemoval));
    }

    @Test
    @DisplayName("Should not remove pipeline pointer if not exist and should return appropriate error code")
    void deletePipelinePointerNotExists() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertNotNull(pipelinePointer);

        Long projectId = pipelineMeta.getProjectId();
        Long pipelineId = 10L; // not existing pipeline pointer

        int pipelinesAmountBeforeRemoval = pipelinesAmount(projectsFile);
        assertAll(() -> assertThrows(PipelineManagementException.class, () -> pipelinePointerIO.deletePipelinePointer(projectId, pipelineId)),
                  () -> {
                      int pipelinesAmountAfterRemoval = pipelinesAmount(projectsFile);
                      assertEquals(1, pipelinesAmountBeforeRemoval);
                      assertEquals(1, pipelinesAmountAfterRemoval);
                  });
    }

    @Test
    @DisplayName("Should not remove pipeline pointer when project not exists")
    void deletePipelinePointerProjectNotExists() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertNotNull(pipelinePointer);

        Long projectId = pipelineMeta.getProjectId() + 1;  // not existing project
        Long pipelineId = 0L;

        int pipelinesAmountBeforeRemoval = pipelinesAmount(projectsFile);
        assertAll(() -> assertThrows(PipelineManagementException.class, () -> pipelinePointerIO.deletePipelinePointer(projectId, pipelineId)),
                () -> {
                    int pipelinesAmountAfterRemoval = pipelinesAmount(projectsFile);
                    assertEquals(1, pipelinesAmountBeforeRemoval);
                    assertEquals(1, pipelinesAmountAfterRemoval);
                });
    }

    @Test
    @DisplayName("Should correctly rename pipeline pointer when one just exists")
    void renamePipelinePointerSuccessTest() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertNotNull(pipelinePointer);

        final String oldPipelineName = pipelineMeta.getName();
        final String newPipelineName = "New pipeline name";
        final Long pipelinePointerId = 0L;
        final Long projectId = 0L;

        PipelinePointer pipelinePointerRenamed = pipelinePointerIO.renamePipelinePointer(projectId, pipelinePointerId, newPipelineName);

        PipelinePointer pipelinePointerChanged = firstPipelinePointer(projectsFile);

        assertAll(() -> assertNotNull(pipelinePointerChanged),
                () -> assertNotNull(pipelinePointerRenamed),
                () -> assertEquals(newPipelineName, pipelinePointerChanged.getName()),
                () -> assertNotEquals(oldPipelineName, pipelinePointerChanged.getName()));
    }

    @Test
    @DisplayName("Should correctly rename tag of pipeline pointer when one just exists")
    void renameTagOfPipelinePointerSuccessTest() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertNotNull(pipelinePointer);

        final String oldTagName = pipelineMeta.getTag();
        final String newTagName = "Tag v2.0";
        final Long pipelinePointerId = 0L;
        final Long projectId = 0L;

        PipelinePointer pipelinePointerEdited = pipelinePointerIO.changePipelinePointerTag(projectId, pipelinePointerId, newTagName);

        PipelinePointer pipelinePointerChanged = firstPipelinePointer(projectsFile);

        assertAll(() -> assertNotNull(pipelinePointerEdited),
                () -> assertEquals(newTagName, pipelinePointerChanged.getTag()),
                () -> assertNotEquals(oldTagName, pipelinePointerChanged.getTag()));
    }

    @Test
    @DisplayName("Should correctly change description of pipeline pointer when one just exists")
    void changeDescriptionOfPipelinePointerSuccessTest() {
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipelineMeta);
        assertNotNull(pipelinePointer);

        final String oldDescription = pipelineMeta.getDescription();
        final String newDescription = "This is new description of this pipeline.";
        final Long pipelinePointerId = 0L;
        final Long projectId = 0L;

        PipelinePointer pipelinePointerEdited = pipelinePointerIO.changePipelinePointerDescription(projectId, pipelinePointerId, newDescription);

        PipelinePointer pipelinePointerChanged = firstPipelinePointer(projectsFile);

        assertAll(() -> assertNotNull(pipelinePointerEdited),
                () -> assertEquals(newDescription, pipelinePointerChanged.getDescription()),
                () -> assertNotEquals(oldDescription, pipelinePointerChanged.getDescription()));
    }

    @Test
    @DisplayName("Should correctly add new project and assign this one to 'other' - default project group")
    void createProjectSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        Project project = projectIO.createNewProject(addProjectRequest);

        assertAll(() -> assertNotNull(projectIO),
                () -> assertNotNull(projectsFile),
                () -> assertNotNull(project));

        Project justAddedProject = firstAddedProject(projectsFile);

        assertAll(() -> assertEquals(1, justAddedProject.getId()),
                () -> assertNotNull(justAddedProject.getCratedDate()),
                () -> assertNull(justAddedProject.getLastModifiedDate()),
                () -> assertEquals(addProjectRequest.getName(), justAddedProject.getName()),
                () -> assertEquals(addProjectRequest.getTag(), justAddedProject.getTag()),
                () -> assertEquals(addProjectRequest.getDescription(), justAddedProject.getDescription()),
                () -> assertEquals(0, justAddedProject.getPipelines().size())
        );
    }

    @Test
    @DisplayName("Should not add new project because one with such name just exists")
    void createProjectNameExistsTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setId(1L);
        projectGroup.setName("Some Project group");
        projectGroup.setProjects(new ArrayList<>(1));
        projectsFile.getProjectGroups().add(projectGroup);

        AddProjectRequest addProjectRequest = prepareAddProjectRequest(projectGroup.getId());

        // first adding
        projectIO.createNewProject(addProjectRequest);

        // second adding
        assertThrows(PipelineManagementException.class, () -> projectIO.createNewProject(addProjectRequest));
    }

    @Test
    @DisplayName("Should not add new project because project group not exists")
    void createProjectNotExistsTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long notExistingProjectGroup = 140L;
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(notExistingProjectGroup);

        assertThrows(PipelineManagementException.class, () -> projectIO.createNewProject(addProjectRequest));
    }

    @Test
    @DisplayName("Should correctly softly remove project if one exists")
    void softRemoveProjectSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        projectIO.createNewProject(addProjectRequest);

        Project projectToRemove = firstAddedProject(projectsFile);
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setProjectId(projectToRemove.getId());
        pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        // In soft removal this pipeline must not be deleted!
        // This pipelinePointer must be moved to 'other' named project with id: 0
        PipelinePointer pipelinePointer = projectToRemove.getPipelines().get(0);

        assertAll(
                // 2 project should be in projectGroup at index = 0
                () -> assertEquals(2, projectsFile.getProjectGroups().get(0).getProjects().size()),
                // First default project should has any pipelinePointers
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()),
                // Second project should has one just added pipelinePointer
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().get(1).getPipelines().size())
        );

        Project project = projectIO.deleteProject(defaultProjectGroupId, projectToRemove.getId(), false);

        assertAll(
                () -> assertNotNull(project),
                // 1 project should be in projectGroup at index = 0, it was 2 project and 1 was removed
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().size()),
                // First default project had 0 pipelinePointer but now has 1,
                // because it was moved from softly deleted project
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()),
                // Second project now not exists because it was removed
                () -> assertThrows(IndexOutOfBoundsException.class, () -> projectsFile.getProjectGroups().get(0).getProjects().get(1).getPipelines()),
                () -> assertEquals(pipelinePointer.getPipelineId(), projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().get(0).getPipelineId())
        );
    }

    @Test
    @DisplayName("Should correctly hard(cascade: project with pipelinePointer) remove project if one exists")
    void hardRemoveProjectSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        projectIO.createNewProject(addProjectRequest);

        Project projectToRemove = firstAddedProject(projectsFile);
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setProjectId(projectToRemove.getId());
        pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        // In soft removal this pipeline must not be deleted!
        // This pipelinePointer must be moved to 'other' named project with id: 0
        PipelinePointer pipelinePointer = projectToRemove.getPipelines().get(0);

        assertAll(
                // 2 project should be in projectGroup at index = 0
                () -> assertEquals(2, projectsFile.getProjectGroups().get(0).getProjects().size()),
                // First default project should has any pipelinePointers
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()),
                // Second project should has one just added pipelinePointer
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().get(1).getPipelines().size())
        );

        Project project = projectIO.deleteProject(defaultProjectGroupId, projectToRemove.getId(), true);

        assertAll(
                () -> assertNotNull(project),
                // 1 project should be in projectGroup at index = 0, it was 2 project and 1 was removed
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().size()),
                // First default project had 0 pipelinePointer and now has 0 too (hard/cascade removal),
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()),
                // Second project now not exists because it was removed
                () -> assertThrows(IndexOutOfBoundsException.class, () -> projectsFile.getProjectGroups().get(0).getProjects().get(1).getPipelines())
        );
    }

    @Test
    @DisplayName("Should not remove project when one not exists")
    void removeProjectNotExistsTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        final Long notExistingProjectId = 140L;

        assertThrows(PipelineManagementException.class, () -> projectIO.deleteProject(defaultProjectGroupId(), notExistingProjectId, true));
    }

    @Test
    @DisplayName("Should cannot remove DEFAULT project")
    void removeProjectDeniedTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();
        Long defaultProjectGroupId = defaultProjectGroupId();

        Project project = projectIO.deleteProject(defaultProjectGroupId, defaultProjectId(), true);

        assertNull(project);
        assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().size());
    }

    @Test
    @DisplayName("Should correctly rename project")
    void renameProjectSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        Project project = projectIO.createNewProject(addProjectRequest);

        assertAll(() -> assertNotNull(projectIO),
                () -> assertNotNull(projectsFile),
                () -> assertNotNull(project));

        Project justAddedProject = firstAddedProject(projectsFile);
        final String projectNewName = "Updated project name";

        Project projectRenamed = projectIO.renameProject(justAddedProject.getId(), projectNewName);

        assertAll(() -> assertNotNull(projectRenamed),
                () -> assertEquals(projectNewName, justAddedProject.getName()));
    }

    @Test
    @DisplayName("Should correctly change tag of project")
    void changeProjectTagSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        Project projectNew = projectIO.createNewProject(addProjectRequest);

        assertAll(() -> assertNotNull(projectIO),
                () -> assertNotNull(projectsFile),
                () -> assertNotNull(projectNew));

        Project justAddedProject = firstAddedProject(projectsFile);
        final String projectNewTag = "Production mode";

        Project projectRenamed = projectIO.changeProjectTag(justAddedProject.getId(), projectNewTag);

        assertAll(() -> assertNotNull(projectRenamed),
                () -> assertEquals(projectNewTag, justAddedProject.getTag()));
    }

    @Test
    @DisplayName("Should correctly change description of project")
    void changeProjectDescriptionSuccessTest() {
        ProjectIO projectIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        Long defaultProjectGroupId = defaultProjectGroupId();
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(defaultProjectGroupId);
        Project projectAdded = projectIO.createNewProject(addProjectRequest);

        assertAll(() -> assertNotNull(projectIO),
                () -> assertNotNull(projectsFile),
                () -> assertNotNull(projectAdded));

        Project justAddedProject = firstAddedProject(projectsFile);
        final String projectNewDescription = "Production mode description";

        Project projectEdited = projectIO.changeProjectDescription(justAddedProject.getId(), projectNewDescription);

        assertAll(() -> assertNotNull(projectEdited),
                () -> assertEquals(projectNewDescription, justAddedProject.getDescription()));
    }

    @Test
    @DisplayName("Should correctly add project group")
    void createProjectGroupTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        AddProjectGroupRequest request = prepareAddProjectGroupRequest();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        ProjectGroup newProjectGroupJustAdded = projectGroupIO.createNewProjectGroup(request);

        assertAll(() -> assertEquals(request.getName(), newProjectGroupJustAdded.getName()),
                () -> assertEquals(request.getTag(), newProjectGroupJustAdded.getTag()),
                () -> assertEquals(request.getDescription(), newProjectGroupJustAdded.getDescription()));

        // id of new added group
        Long projectGroupId = newProjectGroupJustAdded.getId();
        ProjectGroup projectGroup = firstProjectGroup(projectsFile, projectGroupId);

        assertAll(() -> assertEquals(1, projectGroupId),
                () -> assertEquals(request.getName(), projectGroup.getName()),
                () -> assertEquals(request.getTag(), projectGroup.getTag()),
                () -> assertEquals(request.getDescription(), projectGroup.getDescription()),
                () -> assertEquals(0, projectGroup.getProjects().size()));
    }

    @Test
    @DisplayName("Should not add project group because one exists with same name")
    void createProjectGroupJustExistsTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        AddProjectGroupRequest request = prepareAddProjectGroupRequest();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        // add a first time
        ProjectGroup newProjectGroup = projectGroupIO.createNewProjectGroup(request);

        // add a second time
        assertAll(() -> assertThrows(PipelineManagementException.class, () -> projectGroupIO.createNewProjectGroup(request)),
                () -> assertEquals(2, projectsFile.getProjectGroups().size()));
    }

    @Test
    @DisplayName("Should delete project group in soft way without deleting projects correlated with group")
    void deleteProjectGroupSoftSuccessTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        ProjectIO projectIO = ProjectManager.getInstance();
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        AddProjectGroupRequest request = prepareAddProjectGroupRequest();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        // add a project group
        ProjectGroup newProjectGroup = projectGroupIO.createNewProjectGroup(request);

        // add project
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(newProjectGroup.getId());
        projectIO.createNewProject(addProjectRequest);
        Project project = newProjectGroup.getProjects().get(0);

        // add pipeline pointer
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setProjectId(project.getId());
        pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        // assertion before soft removal
        assertAll(() -> assertEquals(2, projectsFile.getProjectGroups().size()),
                () -> assertEquals(1, newProjectGroup.getProjects().get(0).getPipelines().size()),
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().size()),
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()));

        // In soft removal projects correlated with project group and pipelines correlated with
        // these projects must not be removed but moved to default 'other' project group with id = 0
        projectGroupIO.deleteProjectGroup(newProjectGroup.getId(), false);

        // assertions after soft removal
        assertAll(() -> assertEquals(1, newProjectGroup.getProjects().size()),
                () -> assertEquals(1, newProjectGroup.getProjects().get(0).getPipelines().size()),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> newProjectGroup.getProjects().get(1)),
                () -> assertEquals(2, projectsFile.getProjectGroups().get(0).getProjects().size()),
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()));
    }

    @Test
    @DisplayName("Should delete project group in hard way with deleting correlated projects and pipeline pointers")
    void deleteProjectGroupHardSuccessTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        ProjectIO projectIO = ProjectManager.getInstance();
        PipelinePointerIO pipelinePointerIO = ProjectManager.getInstance();
        AddProjectGroupRequest request = prepareAddProjectGroupRequest();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        // add a project group
        ProjectGroup newProjectGroup = projectGroupIO.createNewProjectGroup(request);

        // add project
        AddProjectRequest addProjectRequest = prepareAddProjectRequest(newProjectGroup.getId());
        projectIO.createNewProject(addProjectRequest);
        Project project = newProjectGroup.getProjects().get(0);

        // add pipeline pointer
        EasefileObjectModel.Metadata pipelineMeta = preparePipelineMetadata();
        pipelineMeta.setProjectId(project.getId());
        pipelinePointerIO.createNewPipelinePointer(pipelineMeta);

        // assertion before hard removal
        assertAll(() -> assertEquals(2, projectsFile.getProjectGroups().size()),
                () -> assertEquals(1, newProjectGroup.getProjects().get(0).getPipelines().size()),
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().size()),
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()));

        // In hard removal projects correlated with project group and pipelines correlated with
        // these projects must not be removed but moved to default 'other' project group with id = 0
        projectGroupIO.deleteProjectGroup(newProjectGroup.getId(), true);

        // assertions after hard removal
        assertAll(
                () -> assertEquals(1, newProjectGroup.getProjects().size()),
                () -> assertEquals(1, newProjectGroup.getProjects().get(0).getPipelines().size()),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> newProjectGroup.getProjects().get(1)),
                () -> assertEquals(1, projectsFile.getProjectGroups().get(0).getProjects().size()),
                () -> assertEquals(0, projectsFile.getProjectGroups().get(0).getProjects().get(0).getPipelines().size()));

    }

    @Test
    @DisplayName("Should throw exception when trying to remove project group that not exists")
    void deleteProjectGroupNotExistsTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        final Long notExistingProjectGroupId = 180L;
        assertThrows(PipelineManagementException.class, () -> projectGroupIO.deleteProjectGroup(notExistingProjectGroupId, false));
    }

    @Test
    @DisplayName("Should throw exception when trying to remove default, secured project group")
    void deleteProjectGroupSecuredTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        final Long securedProjectGroupId = defaultProjectGroupId();
        assertThrows(PipelineManagementException.class, () -> projectGroupIO.deleteProjectGroup(securedProjectGroupId, false));
    }

    @Test
    @DisplayName("Should correctly rename project group")
    void renameProjectGroupSuccessTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        AddProjectGroupRequest request = prepareAddProjectGroupRequest();
        ProjectGroup newProjectGroup = projectGroupIO.createNewProjectGroup(request);

        final String oldProjectGroupName = newProjectGroup.getName();
        final String newProjectGroupName = "Changed project group name";

        ProjectGroup renamedProjectGroup = projectGroupIO.renameProjectGroup(newProjectGroup.getId(), newProjectGroupName);

        assertAll(() -> assertNotEquals(oldProjectGroupName, renamedProjectGroup.getName()),
                () -> assertEquals(newProjectGroupName, renamedProjectGroup.getName()));
    }

    @Test
    @DisplayName("Should correctly change tag of project group")
    void changeProjectGroupTagSuccessTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        AddProjectGroupRequest request = prepareAddProjectGroupRequest();
        ProjectGroup newProjectGroup = projectGroupIO.createNewProjectGroup(request);

        final String oldProjectGroupTag = newProjectGroup.getTag();
        final String newProjectGroupTag = "Changed project group tag";

        ProjectGroup renamedProjectGroup = projectGroupIO.changeTag(newProjectGroup.getId(), newProjectGroupTag);

        assertAll(() -> assertNotEquals(oldProjectGroupTag, renamedProjectGroup.getTag()),
                () -> assertEquals(newProjectGroupTag, renamedProjectGroup.getTag()));
    }

    @Test
    @DisplayName("Should correctly change description of project group")
    void changeProjectGroupDescriptionSuccessTest() {
        ProjectGroupIO projectGroupIO = ProjectManager.getInstance();
        ProjectsFile projectsFile = ProjectManager.getInstance().getProjectsFile();

        AddProjectGroupRequest request = prepareAddProjectGroupRequest();
        ProjectGroup newProjectGroup = projectGroupIO.createNewProjectGroup(request);

        final String oldProjectGroupDescription = newProjectGroup.getDescription();
        final String newProjectGroupDescription = "Changed project group description";

        ProjectGroup renamedProjectGroup = projectGroupIO.changeDescription(newProjectGroup.getId(), newProjectGroupDescription);

        assertAll(() -> assertNotEquals(oldProjectGroupDescription, renamedProjectGroup.getDescription()),
                () -> assertEquals(newProjectGroupDescription, renamedProjectGroup.getDescription()));
    }


    private int pipelinesAmount(ProjectsFile projectsFile) {
        return projectsFile.getProjectGroups()
                .get(0).getProjects()
                .get(0).getPipelines()
                .size();
    }

    private PipelinePointer firstPipelinePointer(ProjectsFile projectsFile) {
        return projectsFile.getProjectGroups()
                .get(0).getProjects()
                .get(0).getPipelines()
                .get(0);
    }

    private Project firstAddedProject(ProjectsFile projectsFile) {
        return projectsFile.getProjectGroups()
                .get(0)
                .getProjects()
                .get(1); // get first project in projectGroup, not zero because on zero index is default project
    }

    private ProjectGroup firstProjectGroup(ProjectsFile projectsFile, Long projectGroupId) {
        return projectsFile.getProjectGroups().stream()
                .filter(projectGroup -> projectGroup.getId().equals(projectGroupId))
                .findFirst()
                .orElseThrow();
    }
}