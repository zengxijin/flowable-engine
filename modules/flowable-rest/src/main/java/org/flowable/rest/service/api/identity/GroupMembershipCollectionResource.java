/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.rest.service.api.identity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.*;

import org.flowable.engine.common.api.FlowableIllegalArgumentException;
import org.flowable.idm.api.Group;
import org.flowable.rest.exception.ActivitiConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Groups" }, description = "Manage Groups")
public class GroupMembershipCollectionResource extends BaseGroupResource {

  @ApiOperation(value = "Add a member to a group", tags = {"Groups"})
  @ApiResponses(value = {
          @ApiResponse(code = 201, message = "Indicates the group was found and the member has been added."),
          @ApiResponse(code = 400, message = "Indicates the userId was not included in the request body."),
          @ApiResponse(code = 404, message = "Indicates the requested group was not found."),
          @ApiResponse(code = 409, message = "Indicates the requested user is already a member of the group.")
  })
  @RequestMapping(value = "/identity/groups/{groupId}/members", method = RequestMethod.POST, produces = "application/json")
  public MembershipResponse createMembership(@ApiParam(name = "groupId") @PathVariable String groupId, @RequestBody MembershipRequest memberShip, HttpServletRequest request, HttpServletResponse response) {

    Group group = getGroupFromRequest(groupId);

    if (memberShip.getUserId() == null) {
      throw new FlowableIllegalArgumentException("UserId cannot be null.");
    }

    // Check if user is member of group since API doesn't return typed
    // exception
    if (identityService.createUserQuery().memberOfGroup(group.getId()).userId(memberShip.getUserId()).count() > 0) {

      throw new ActivitiConflictException("User '" + memberShip.getUserId() + "' is already part of group '" + group.getId() + "'.");
    }

    identityService.createMembership(memberShip.getUserId(), group.getId());
    response.setStatus(HttpStatus.CREATED.value());

    return restResponseFactory.createMembershipResponse(memberShip.getUserId(), group.getId());
  }
}
