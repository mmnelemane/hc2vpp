/*
 * Copyright (c) 2016 Cisco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fd.hc2vpp.acl.util.acl;

import io.fd.hc2vpp.acl.util.AclContextManager;
import io.fd.hc2vpp.acl.util.ace.AceConverter;
import io.fd.hc2vpp.common.translate.util.JvppReplyConsumer;
import io.fd.honeycomb.translate.MappingContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.vpp.jvpp.acl.dto.AclAddReplace;
import io.fd.vpp.jvpp.acl.dto.AclAddReplaceReply;
import io.fd.vpp.jvpp.acl.dto.AclDel;
import io.fd.vpp.jvpp.acl.dto.MacipAclAdd;
import io.fd.vpp.jvpp.acl.dto.MacipAclAddReply;
import io.fd.vpp.jvpp.acl.dto.MacipAclDel;
import io.fd.vpp.jvpp.acl.future.FutureJVppAclFacade;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160708.access.lists.Acl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.access.control.list.rev160708.access.lists.acl.access.list.entries.Ace;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Write standard and mac-ip acls
 */
public interface AclWriter extends AclDataExtractor, AceConverter, JvppReplyConsumer {

    int ACL_INDEX_CREATE_NEW = -1;

    default void addStandardAcl(@Nonnull final FutureJVppAclFacade futureFacade,
                                @Nonnull final InstanceIdentifier<Acl> id, @Nonnull final Acl acl,
                                @Nonnull final AclContextManager standardAclContext,
                                @Nonnull final MappingContext mappingContext) throws WriteFailedException {

        final AclAddReplace request = new AclAddReplace();

        request.tag = getAclTag(acl);
        request.aclIndex = ACL_INDEX_CREATE_NEW;

        final List<Ace> aces = getAces(acl);
        request.r = toStandardAclRules(aces);
        request.count = request.r.length;

        final AclAddReplaceReply reply =
                getReplyForWrite(futureFacade.aclAddReplace(request).toCompletableFuture(), id);

        // maps new acl to returned index
        standardAclContext.addAcl(reply.aclIndex, acl.getAclName(), aces, mappingContext);
    }

    // according to vpp team, this was tested extensively, and should work
    default void updateStandardAcl(@Nonnull final FutureJVppAclFacade futureFacade,
                                   @Nonnull final InstanceIdentifier<Acl> id, @Nonnull final Acl acl,
                                   @Nonnull final AclContextManager standardAclContext,
                                   @Nonnull final MappingContext mappingContext) throws WriteFailedException {

        final AclAddReplace request = new AclAddReplace();

        request.tag = getAclTag(acl);
        // by setting existing index, request is resolved as update
        request.aclIndex = standardAclContext.getAclIndex(acl.getAclName(), mappingContext);

        final List<Ace> aces = getAces(acl);
        request.r = toStandardAclRules(aces);
        request.count = request.r.length;

        final AclAddReplaceReply reply = getReplyForWrite(futureFacade.aclAddReplace(request).toCompletableFuture(), id);

        // overwrites existing acl metadata (aces might have been changed):
        standardAclContext.addAcl(reply.aclIndex, acl.getAclName(), aces, mappingContext);
    }

    default void deleteStandardAcl(@Nonnull final FutureJVppAclFacade futureFacade,
                                   @Nonnull final InstanceIdentifier<Acl> id, @Nonnull final Acl acl,
                                   @Nonnull final AclContextManager standardAclContext,
                                   @Nonnull final MappingContext mappingContext) throws WriteFailedException {

        final AclDel request = new AclDel();
        final String aclName = acl.getAclName();
        request.aclIndex = standardAclContext.getAclIndex(aclName, mappingContext);

        getReplyForDelete(futureFacade.aclDel(request).toCompletableFuture(), id);

        // removes mapping after successful delete
        standardAclContext.removeAcl(aclName, mappingContext);
    }

    default void addMacIpAcl(@Nonnull final FutureJVppAclFacade futureFacade,
                             @Nonnull final InstanceIdentifier<Acl> id, @Nonnull final Acl acl,
                             @Nonnull final AclContextManager macIpAclContext,
                             @Nonnull final MappingContext mappingContext) throws WriteFailedException {
        final MacipAclAdd request = new MacipAclAdd();

        request.tag = getAclTag(acl);

        final List<Ace> aces = getAces(acl);
        request.r = toMacIpAclRules(aces);
        request.count = request.r.length;

        final MacipAclAddReply reply = getReplyForWrite(futureFacade.macipAclAdd(request).toCompletableFuture(), id);

        // map mac-ip acl to returned index
        macIpAclContext.addAcl(reply.aclIndex, acl.getAclName(), aces, mappingContext);
    }

    default void deleteMacIpAcl(@Nonnull final FutureJVppAclFacade futureFacade,
                                @Nonnull final InstanceIdentifier<Acl> id, @Nonnull final Acl acl,
                                @Nonnull final AclContextManager macIpAclContext,
                                @Nonnull final MappingContext mappingContext) throws WriteFailedException {
        final MacipAclDel request = new MacipAclDel();
        final String aclName = acl.getAclName();
        request.aclIndex = macIpAclContext.getAclIndex(aclName, mappingContext);

        getReplyForDelete(futureFacade.macipAclDel(request).toCompletableFuture(), id);

        macIpAclContext.removeAcl(aclName, mappingContext);
    }


}
