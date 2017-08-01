/*
 * Copyright (c) 2017 Cisco and/or its affiliates.
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

package io.fd.hc2vpp.nat.read.ifc;

import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.spi.read.Initialized;
import io.fd.vpp.jvpp.snat.future.FutureJVppSnatFacade;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang._interface.nat.rev170801._interface.nat.attributes.Nat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang._interface.nat.rev170801._interface.nat.attributes.NatBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang._interface.nat.rev170801._interface.nat.attributes.nat.Outbound;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang._interface.nat.rev170801._interface.nat.attributes.nat.OutboundBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.subinterface.nat.rev170615.NatSubinterfaceAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.SubinterfaceAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces._interface.SubInterfaces;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces._interface.sub.interfaces.SubInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces._interface.sub.interfaces.SubInterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SubInterfaceOutboundNatCustomizer extends AbstractSubInterfaceNatCustomizer<Outbound, OutboundBuilder> {

    private static final Logger LOG = LoggerFactory.getLogger(SubInterfaceOutboundNatCustomizer.class);

    SubInterfaceOutboundNatCustomizer(@Nonnull final FutureJVppSnatFacade jvppSnat,
                                      @Nonnull final NamingContext ifcContext) {
        super(jvppSnat, ifcContext);
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }

    @Override
    boolean isExpectedNatType(final int isInside) {
        return isInside == 0;
    }

    @Override
    void setPostRouting(final OutboundBuilder builder) {
        builder.setPostRouting(true);
    }

    @Nonnull
    @Override
    public OutboundBuilder getBuilder(@Nonnull final InstanceIdentifier<Outbound> id) {
        return new OutboundBuilder();
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> parentBuilder, @Nonnull final Outbound readValue) {
        ((NatBuilder) parentBuilder).setOutbound(readValue);
    }

    @Nonnull
    @Override
    public Initialized<? extends DataObject> init(@Nonnull final InstanceIdentifier<Outbound> id,
                                                  @Nonnull final Outbound readValue,
                                                  @Nonnull final ReadContext ctx) {
        final InstanceIdentifier<Outbound> cfgId =
                InstanceIdentifier.create(Interfaces.class)
                        .child(Interface.class,
                                new InterfaceKey(id.firstKeyOf(
                                        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.Interface.class)
                                        .getName()))
                        .augmentation(SubinterfaceAugmentation.class)
                        .child(SubInterfaces.class)
                        .child(SubInterface.class,
                                new SubInterfaceKey(id.firstKeyOf(
                                        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev170607.interfaces.state._interface.sub.interfaces.SubInterface.class)
                                        .getIdentifier()))
                        .augmentation(NatSubinterfaceAugmentation.class)
                        .child(Nat.class)
                        .child(Outbound.class);
        return Initialized.create(cfgId, readValue);
    }
}
