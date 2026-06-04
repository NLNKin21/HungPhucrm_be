package com.hungphu.crm.features.task.mapper;

import com.hungphu.crm.features.task.dto.TaskResponse;
import com.hungphu.crm.features.task.entity.Task;
import com.hungphu.crm.features.task.entity.TaskEvidence;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-04T22:19:36+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public TaskResponse toResponse(Task task) {
        if ( task == null ) {
            return null;
        }

        TaskResponse.TaskResponseBuilder taskResponse = TaskResponse.builder();

        taskResponse.assignedBy( toUserInfo( task.getAssignedBy() ) );
        taskResponse.assignedTo( toUserInfo( task.getAssignedTo() ) );
        taskResponse.supervisor( toUserInfo( task.getSupervisor() ) );
        taskResponse.completedAt( task.getCompletedAt() );
        taskResponse.createdAt( task.getCreatedAt() );
        taskResponse.deadline( task.getDeadline() );
        taskResponse.evidences( taskEvidenceListToEvidenceInfoList( task.getEvidences() ) );
        taskResponse.id( task.getId() );
        taskResponse.rejectionReason( task.getRejectionReason() );
        taskResponse.siteAddress( task.getSiteAddress() );
        taskResponse.status( task.getStatus() );
        taskResponse.taskType( task.getTaskType() );
        taskResponse.title( task.getTitle() );
        taskResponse.updatedAt( task.getUpdatedAt() );

        taskResponse.members( toMemberInfos(task.getMembers()) );

        return taskResponse.build();
    }

    protected List<TaskResponse.EvidenceInfo> taskEvidenceListToEvidenceInfoList(List<TaskEvidence> list) {
        if ( list == null ) {
            return null;
        }

        List<TaskResponse.EvidenceInfo> list1 = new ArrayList<TaskResponse.EvidenceInfo>( list.size() );
        for ( TaskEvidence taskEvidence : list ) {
            list1.add( toEvidenceInfo( taskEvidence ) );
        }

        return list1;
    }
}
