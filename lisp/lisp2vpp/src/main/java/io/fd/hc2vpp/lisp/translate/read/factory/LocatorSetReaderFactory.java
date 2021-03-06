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

package io.fd.hc2vpp.lisp.translate.read.factory;


import io.fd.hc2vpp.lisp.translate.AbstractLispInfraFactoryBase;
import io.fd.hc2vpp.lisp.translate.read.InterfaceCustomizer;
import io.fd.hc2vpp.lisp.translate.read.LocatorSetCustomizer;
import io.fd.honeycomb.translate.impl.read.GenericInitListReader;
import io.fd.honeycomb.translate.read.ReaderFactory;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev170911.lisp.feature.data.grouping.LispFeatureData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev170911.locator.sets.grouping.LocatorSets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev170911.locator.sets.grouping.LocatorSetsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev170911.locator.sets.grouping.locator.sets.LocatorSet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev170911.locator.sets.grouping.locator.sets.locator.set.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


/**
 * Produces reader for {@link LocatorSets} <br> and all its inhired child readers.
 */
public class LocatorSetReaderFactory extends AbstractLispInfraFactoryBase implements ReaderFactory {


    @Override
    public void init(@Nonnull final ModifiableReaderRegistryBuilder registry) {
        InstanceIdentifier<LocatorSets> locatorSetsInstanceIdentifier =
                LISP_OPERATIONAL_IDENTIFIER.child(LispFeatureData.class).child(LocatorSets.class);
        InstanceIdentifier<LocatorSet> locatorSetInstanceIdentifier =
                locatorSetsInstanceIdentifier.child(LocatorSet.class);

        registry.addStructuralReader(locatorSetsInstanceIdentifier, LocatorSetsBuilder.class);
        registry.add(new GenericInitListReader<>(locatorSetInstanceIdentifier,
                new LocatorSetCustomizer(vppApi, lispStateCheckService)));
        registry.add(new GenericInitListReader<>(locatorSetInstanceIdentifier.child(Interface.class),
                new InterfaceCustomizer(vppApi, interfaceContext, locatorSetContext)));
    }
}
