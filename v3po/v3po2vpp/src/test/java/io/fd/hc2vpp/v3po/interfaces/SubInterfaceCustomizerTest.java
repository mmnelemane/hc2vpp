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

package io.fd.hc2vpp.v3po.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.fd.hc2vpp.common.test.write.WriterCustomizerTest;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.vpp.jvpp.VppBaseCallException;
import io.fd.vpp.jvpp.core.dto.CreateSubif;
import io.fd.vpp.jvpp.core.dto.CreateSubifReply;
import io.fd.vpp.jvpp.core.dto.SwInterfaceSetFlags;
import io.fd.vpp.jvpp.core.dto.SwInterfaceSetFlagsReply;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.CVlan;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.Dot1qTagVlanType;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.Dot1qVlanId;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.SVlan;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.dot1q.tag.or.any.Dot1qTag;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.dot1q.tag.or.any.Dot1qTagBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.SubinterfaceAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607._802dot1ad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces._interface.SubInterfaces;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces._interface.sub.interfaces.SubInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces._interface.sub.interfaces.SubInterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces._interface.sub.interfaces.SubInterfaceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.match.attributes.match.type.vlan.tagged.VlanTaggedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.sub._interface.base.attributes.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.sub._interface.base.attributes.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.sub._interface.base.attributes.TagsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.sub._interface.base.attributes.tags.Tag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.sub._interface.base.attributes.tags.TagBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.sub._interface.base.attributes.tags.TagKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubInterfaceCustomizerTest extends WriterCustomizerTest {

    private static final String IFC_TEST_INSTANCE = "ifc-test-instance";
    private static final String SUPER_IF_NAME = "local0";
    private static final int SUPER_IF_ID = 1;
    private static final String SUB_IFACE_NAME = "local0.11";
    private static final int SUBIF_INDEX = 11;
    private static final short STAG_ID = 100;
    private static final short CTAG_ID = 200;
    private static final short CTAG_ANY_ID = 0; // only the *IdAny flag is set
    private final Tag STAG_100;
    private final Tag CTAG_200;
    private final Tag CTAG_ANY;
    private NamingContext namingContext;
    private SubInterfaceCustomizer customizer;

    public SubInterfaceCustomizerTest() {
        STAG_100 = generateTag((short) 0, SVlan.class, new Dot1qTag.VlanId(new Dot1qVlanId((int) STAG_ID)));
        CTAG_200 = generateTag((short) 1, CVlan.class, new Dot1qTag.VlanId(new Dot1qVlanId(200)));
        CTAG_ANY = generateTag((short) 1, CVlan.class, new Dot1qTag.VlanId(Dot1qTag.VlanId.Enumeration.Any));
    }

    private static Tag generateTag(final short index, final Class<? extends Dot1qTagVlanType> tagType,
                                   final Dot1qTag.VlanId vlanId) {
        TagBuilder tag = new TagBuilder();
        tag.setIndex(index);
        tag.setKey(new TagKey(index));
        final Dot1qTagBuilder dtag = new Dot1qTagBuilder();
        dtag.setTagType(tagType);
        dtag.setVlanId(vlanId);
        tag.setDot1qTag(dtag.build());
        return tag.build();
    }

    private static Match generateMatch() {
        final MatchBuilder match = new MatchBuilder();
        final VlanTaggedBuilder tagged = new VlanTaggedBuilder();
        tagged.setMatchExactTags(true);
        match.setMatchType(
            new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.match.attributes.match.type.VlanTaggedBuilder()
                .setVlanTagged(tagged.build()).build());
        return match.build();
    }

    @Override
    public void setUpTest() throws Exception {
        namingContext = new NamingContext("generatedSubInterfaceName", IFC_TEST_INSTANCE);
        customizer = new SubInterfaceCustomizer(api, namingContext);
        defineMapping(mappingContext, SUB_IFACE_NAME, SUBIF_INDEX, IFC_TEST_INSTANCE);
        defineMapping(mappingContext, SUPER_IF_NAME, SUPER_IF_ID, IFC_TEST_INSTANCE);
    }

    private SubInterface generateSubInterface(final boolean enabled, final List<Tag> tagList) {
        SubInterfaceBuilder builder = new SubInterfaceBuilder();
        builder.setVlanType(_802dot1ad.class);
        builder.setIdentifier(11L);
        final TagsBuilder tags = new TagsBuilder();

        tags.setTag(tagList);

        builder.setTags(tags.build());

        builder.setMatch(generateMatch());
        builder.setEnabled(enabled);
        return builder.build();
    }

    private CreateSubif generateSubInterfaceRequest(final int superIfId, final short innerVlanId,
                                                    final boolean isInnerAny) {
        CreateSubif request = new CreateSubif();
        request.subId = 11;
        request.swIfIndex = superIfId;
        request.twoTags = 1;
        request.innerVlanId = innerVlanId;
        request.innerVlanIdAny = (byte) (isInnerAny
            ? 1
            : 0);
        request.dot1Ad = 1;
        request.outerVlanId = STAG_ID;
        request.exactMatch = 1;
        return request;
    }

    private SwInterfaceSetFlags generateSwInterfaceEnableRequest(final int swIfIndex) {
        SwInterfaceSetFlags request = new SwInterfaceSetFlags();
        request.swIfIndex = swIfIndex;
        request.adminUpDown = 1;
        return request;
    }

    private InstanceIdentifier<SubInterface> getSubInterfaceId(final String name, final long index) {
        return InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(name)).augmentation(
            SubinterfaceAugmentation.class).child(SubInterfaces.class)
            .child(SubInterface.class, new SubInterfaceKey(index));
    }

    private void whenCreateSubifThenSuccess() {
        doReturn(future(new CreateSubifReply())).when(api).createSubif(any(CreateSubif.class));
    }

    /**
     * Failure response send
     */
    private void whenCreateSubifThenFailure() {
        doReturn(failedFuture()).when(api).createSubif(any(CreateSubif.class));
    }

    private void whenSwInterfaceSetFlagsThenSuccess() {
        doReturn(future(new SwInterfaceSetFlagsReply())).when(api).swInterfaceSetFlags(any(SwInterfaceSetFlags.class));
    }

    private SwInterfaceSetFlags verifySwInterfaceSetFlagsWasInvoked(final SwInterfaceSetFlags expected)
        throws VppBaseCallException {
        ArgumentCaptor<SwInterfaceSetFlags> argumentCaptor = ArgumentCaptor.forClass(SwInterfaceSetFlags.class);
        verify(api).swInterfaceSetFlags(argumentCaptor.capture());
        final SwInterfaceSetFlags actual = argumentCaptor.getValue();

        assertEquals(expected.swIfIndex, actual.swIfIndex);
        assertEquals(expected.adminUpDown, actual.adminUpDown);
        return actual;
    }

    @Test
    public void testCreateTwoTags() throws Exception {
        final SubInterface subInterface = generateSubInterface(false, Arrays.asList(STAG_100, CTAG_200));
        final InstanceIdentifier<SubInterface> id = getSubInterfaceId(SUPER_IF_NAME, SUBIF_INDEX);

        whenCreateSubifThenSuccess();
        whenSwInterfaceSetFlagsThenSuccess();

        customizer.writeCurrentAttributes(id, subInterface, writeContext);

        verify(api).createSubif(generateSubInterfaceRequest(SUPER_IF_ID, CTAG_ID, false));
        verify(mappingContext)
            .put(eq(mappingIid(SUB_IFACE_NAME, IFC_TEST_INSTANCE)), eq(
                mapping(SUB_IFACE_NAME, 0).get()));
    }

    @Test
    public void testCreateDot1qAnyTag() throws Exception {
        final SubInterface subInterface = generateSubInterface(false, Arrays.asList(STAG_100, CTAG_ANY));
        final InstanceIdentifier<SubInterface> id = getSubInterfaceId(SUPER_IF_NAME, SUBIF_INDEX);

        whenCreateSubifThenSuccess();
        whenSwInterfaceSetFlagsThenSuccess();

        customizer.writeCurrentAttributes(id, subInterface, writeContext);

        verify(api).createSubif(generateSubInterfaceRequest(SUPER_IF_ID, CTAG_ANY_ID, true));
        verify(mappingContext)
            .put(eq(mappingIid(SUB_IFACE_NAME, IFC_TEST_INSTANCE)), eq(
                mapping(SUB_IFACE_NAME, 0).get()));
    }

    @Test
    public void testCreateFailed() throws Exception {
        final SubInterface subInterface = generateSubInterface(false, Arrays.asList(STAG_100, CTAG_200));
        final InstanceIdentifier<SubInterface> id = getSubInterfaceId(SUPER_IF_NAME, SUBIF_INDEX);

        whenCreateSubifThenFailure();

        try {
            customizer.writeCurrentAttributes(id, subInterface, writeContext);
        } catch (WriteFailedException.CreateFailedException e) {
            assertTrue(e.getCause() instanceof VppBaseCallException);
            verify(api).createSubif(generateSubInterfaceRequest(SUPER_IF_ID, CTAG_ID, false));
            verify(mappingContext, times(0)).put(
                eq(mappingIid(SUPER_IF_NAME, IFC_TEST_INSTANCE)),
                eq(mapping(SUPER_IF_NAME, 0).get()));
            return;
        }
        fail("WriteFailedException.CreateFailedException was expected");
    }

    @Test
    public void testUpdate() throws Exception {
        final List<Tag> tags = Arrays.asList(STAG_100, CTAG_200);
        final SubInterface before = generateSubInterface(false, tags);
        final SubInterface after = generateSubInterface(true, tags);
        final InstanceIdentifier<SubInterface> id = getSubInterfaceId(SUPER_IF_NAME, SUBIF_INDEX);

        whenSwInterfaceSetFlagsThenSuccess();
        customizer.updateCurrentAttributes(id, before, after, writeContext);

        verifySwInterfaceSetFlagsWasInvoked(generateSwInterfaceEnableRequest(SUBIF_INDEX));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelete() throws Exception {
        final SubInterface subInterface = generateSubInterface(false, Arrays.asList(STAG_100, CTAG_200));
        customizer.deleteCurrentAttributes(null, subInterface, writeContext);
    }
}