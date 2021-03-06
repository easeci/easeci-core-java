package io.easeci.core.workspace.projects;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
class ProjectsFile implements Serializable {
    public static final Long INITIAL_PROJECT_ID = 0L;

    private List<ProjectGroup> projectGroups;

    public static ProjectsFile empty() {
        ProjectsFile projectsFile = new ProjectsFile();
        projectsFile.setProjectGroups(new ArrayList<>(0));
        return projectsFile;
    }

    public static ProjectsFile initialState() {
        ProjectsFile projectsFile = new ProjectsFile();
        projectsFile.setProjectGroups(
                Collections.singletonList(
                            ProjectGroup.builder()
                                    .id(INITIAL_PROJECT_ID)
                                    .cratedDate(new Date())
                                    .name("other")
                                    .projects(Collections.singletonList(Project.builder()
                                            .id(INITIAL_PROJECT_ID)
                                            .cratedDate(new Date())
                                            .name("other")
                                            .pipelines(new ArrayList<>(0))
                                            .build()))
                                    .description("Unassigned projects")
                                    .build()
                        )
        );
        return projectsFile;
    }

    public boolean join(PipelinePointer pointer) {
        return projectGroups.stream()
                .flatMap(projectGroup -> projectGroup.getProjects().stream())
                .filter(project -> project.getId().equals(pointer.getProjectId()))
                .map(project -> project.getPipelines().add(pointer))
                .findFirst()
                .orElse(false);
    }

    public static Long defaultProjectGroupId() {
        return INITIAL_PROJECT_ID;
    }

    public static Long defaultProjectId() {
        return INITIAL_PROJECT_ID;
    }
}
