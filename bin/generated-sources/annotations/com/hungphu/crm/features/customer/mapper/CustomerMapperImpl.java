package com.hungphu.crm.features.customer.mapper;

import com.hungphu.crm.features.customer.dto.AssignedUserInfo;
import com.hungphu.crm.features.customer.dto.CustomerResponse;
import com.hungphu.crm.features.customer.entity.Customer;
import com.hungphu.crm.features.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-05T17:10:03+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public CustomerResponse toResponse(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerResponse.CustomerResponseBuilder customerResponse = CustomerResponse.builder();

        customerResponse.address( customer.getAddress() );
        customerResponse.assignedUser( toAssignedUserInfo( customer.getAssignedUser() ) );
        customerResponse.createdAt( customer.getCreatedAt() );
        customerResponse.elevatorType( customer.getElevatorType() );
        customerResponse.fullName( customer.getFullName() );
        customerResponse.id( customer.getId() );
        customerResponse.phone( customer.getPhone() );
        customerResponse.projectType( customer.getProjectType() );
        customerResponse.updatedAt( customer.getUpdatedAt() );

        return customerResponse.build();
    }

    @Override
    public AssignedUserInfo toAssignedUserInfo(User user) {
        if ( user == null ) {
            return null;
        }

        AssignedUserInfo.AssignedUserInfoBuilder assignedUserInfo = AssignedUserInfo.builder();

        assignedUserInfo.fullName( user.getFullName() );
        assignedUserInfo.id( user.getId() );

        return assignedUserInfo.build();
    }
}
