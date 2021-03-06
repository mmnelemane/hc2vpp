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

package io.fd.hc2vpp.routing.write;


import com.google.common.collect.ImmutableList;
import io.fd.hc2vpp.common.test.write.WriterCustomizerTest;
import io.fd.hc2vpp.common.translate.util.MultiNamingContext;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.routing.helpers.ClassifyTableTestHelper;
import io.fd.hc2vpp.routing.helpers.RoutingRequestTestHelper;
import io.fd.hc2vpp.routing.helpers.SchemaContextTestHelper;
import io.fd.hc2vpp.routing.naming.Ipv4RouteNamesFactory;
import io.fd.hc2vpp.vpp.classifier.context.VppClassifierContextManager;
import io.fd.honeycomb.test.tools.HoneycombTestRunner;
import io.fd.honeycomb.test.tools.annotations.InjectTestData;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.vpp.jvpp.core.dto.IpAddDelRoute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ipv4.unicast.routing.rev170917.StaticRoutes1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ipv4.unicast.routing.rev170917.routing.routing.instance.routing.protocols.routing.protocol._static.routes.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ipv4.unicast.routing.rev170917.routing.routing.instance.routing.protocols.routing.protocol._static.routes.ipv4.Route;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ipv4.unicast.routing.rev170917.routing.routing.instance.routing.protocols.routing.protocol._static.routes.ipv4.RouteBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ipv4.unicast.routing.rev170917.routing.routing.instance.routing.protocols.routing.protocol._static.routes.ipv4.RouteKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ipv4.unicast.routing.rev170917.routing.routing.instance.routing.protocols.routing.protocol._static.routes.ipv4.route.next.hop.options.TableLookupBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ipv4.unicast.routing.rev170917.routing.routing.instance.routing.protocols.routing.protocol._static.routes.ipv4.route.next.hop.options.table.lookup.TableLookupParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev140524.routing.routing.instance.RoutingProtocols;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev140524.routing.routing.instance.routing.protocols.RoutingProtocol;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev140524.routing.routing.instance.routing.protocols.RoutingProtocolKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.routing.rev140524.routing.routing.instance.routing.protocols.routing.protocol.StaticRoutes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.vpp.routing.rev170917.VniReference;



import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import static io.fd.hc2vpp.routing.Ipv4RouteData.FIRST_ADDRESS_AS_ARRAY;
import static io.fd.hc2vpp.routing.Ipv4RouteData.SECOND_ADDRESS_AS_ARRAY;
import static io.fd.hc2vpp.routing.helpers.InterfaceTestHelper.INTERFACE_INDEX;
import static io.fd.hc2vpp.routing.helpers.InterfaceTestHelper.INTERFACE_NAME;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(HoneycombTestRunner.class)
public class Ipv4RouteCustomizerTest extends WriterCustomizerTest
        implements ClassifyTableTestHelper, RoutingRequestTestHelper, SchemaContextTestHelper {

    private static final int ROUTE_PROTOCOL_INDEX = 1;
    @Captor
    private ArgumentCaptor<IpAddDelRoute> requestCaptor;

    @Mock
    private VppClassifierContextManager classifyManager;

    @Mock
    private MultiNamingContext routeHopContext;

    private Ipv4RouteCustomizer customizer;
    private InstanceIdentifier<Route> validId;
    private Ipv4RouteNamesFactory namesFactory;
    private NamingContext routingProtocolContext;

    @Override
    protected void setUpTest() throws Exception {
        NamingContext interfaceContext = new NamingContext("interface", "interface-context");
        routingProtocolContext = new NamingContext("routing-protocol", "routing-protocol-context");
        customizer = new Ipv4RouteCustomizer(api, interfaceContext,
                new NamingContext("route", "route-context"),
                routingProtocolContext, routeHopContext, classifyManager);

        validId = InstanceIdentifier.create(RoutingProtocols.class)
                .child(RoutingProtocol.class, new RoutingProtocolKey(ROUTE_PROTOCOL_NAME))
                .child(StaticRoutes.class)
                .augmentation(StaticRoutes1.class)
                .child(Ipv4.class)
                .child(Route.class);

        namesFactory = new Ipv4RouteNamesFactory(interfaceContext, routingProtocolContext);

        defineMapping(mappingContext, INTERFACE_NAME, INTERFACE_INDEX, "interface-context");
        defineMapping(mappingContext, ROUTE_PROTOCOL_NAME, ROUTE_PROTOCOL_INDEX, "routing-protocol-context");
        addMapping(classifyManager, CLASSIFY_TABLE_NAME, CLASSIFY_TABLE_INDEX, mappingContext);
        whenAddRouteThenSuccess(api);
    }

    @Test
    public void testWriteSingleHop(
            @InjectTestData(resourcePath = "/ipv4/simplehop/simpleHopRouteWithClassifier.json", id = STATIC_ROUTE_PATH) StaticRoutes route)
            throws WriteFailedException {
        final Route route1 = getIpv4RouteWithId(route, 1L);
        noMappingDefined(mappingContext, namesFactory.uniqueRouteName(ROUTE_PROTOCOL_NAME, route1), "route-context");

        customizer.writeCurrentAttributes(validId, route1, writeContext);
        verifyInvocation(1, ImmutableList
                        .of(desiredFlaglessResult(1, 0, 0, FIRST_ADDRESS_AS_ARRAY, 24,
                                SECOND_ADDRESS_AS_ARRAY, INTERFACE_INDEX, 0, ROUTE_PROTOCOL_INDEX, 1, 0,
                                CLASSIFY_TABLE_INDEX, 1)),
                api, requestCaptor);
    }

    //TODO - https://jira.fd.io/browse/HONEYCOMB-396
    @Test
    public void testWriteTableLookup() throws WriteFailedException {
        final Route route = new RouteBuilder()
                .setKey(new RouteKey(2L))
                .setDestinationPrefix(new Ipv4Prefix("192.168.2.1/24"))
                .setNextHopOptions(new TableLookupBuilder()
                        .setTableLookupParams(new TableLookupParamsBuilder()
                                .setSecondaryVrf(new VniReference(4L))
                                .build())
                        .build())
                .build();
        noMappingDefined(mappingContext, namesFactory.uniqueRouteName(ROUTE_PROTOCOL_NAME, route), "route-context");
        customizer.writeCurrentAttributes(validId, route, writeContext);
        verifyInvocation(1, ImmutableList
                        .of(desiredFlaglessResult(1, 0, 0, FIRST_ADDRESS_AS_ARRAY, 24,
                                new byte[4], ~0, 0, ROUTE_PROTOCOL_INDEX, 1, 4,
                                0, 0)),
                api, requestCaptor);
    }

    @Test
    public void testWriteHopList(
            @InjectTestData(resourcePath = "/ipv4/multihop/multiHopRouteWithClassifier.json", id = STATIC_ROUTE_PATH) StaticRoutes route)
            throws WriteFailedException {
        final Route route1 = getIpv4RouteWithId(route, 1L);
        noMappingDefined(mappingContext, namesFactory.uniqueRouteName(ROUTE_PROTOCOL_NAME, route1), "route-context");

        customizer.writeCurrentAttributes(validId, route1, writeContext);
        verifyInvocation(2,
                ImmutableList.of(
                        desiredFlaglessResult(1, 0, 1, FIRST_ADDRESS_AS_ARRAY, 24,
                                FIRST_ADDRESS_AS_ARRAY, INTERFACE_INDEX, 2, ROUTE_PROTOCOL_INDEX, 1, 0,
                                CLASSIFY_TABLE_INDEX, 1),
                        desiredFlaglessResult(1, 0, 1, FIRST_ADDRESS_AS_ARRAY, 24,
                                SECOND_ADDRESS_AS_ARRAY, INTERFACE_INDEX, 3, ROUTE_PROTOCOL_INDEX, 1, 0,
                                CLASSIFY_TABLE_INDEX, 1)), api,
                requestCaptor);

        verify(routeHopContext, times(1))
                .addChild(
                        namesFactory.uniqueRouteName(ROUTE_PROTOCOL_NAME, route1), 1,
                        namesFactory.uniqueRouteHopName(getHopWithId(route1, 1)),
                        mappingContext);
        verify(routeHopContext, times(1))
                .addChild(
                        namesFactory.uniqueRouteName(ROUTE_PROTOCOL_NAME, route1), 2,
                        namesFactory.uniqueRouteHopName(getHopWithId(route1, 2)),
                        mappingContext);
    }

    @Test
    public void testWriteSpecialHop(
            @InjectTestData(resourcePath = "/ipv4/specialhop/specialHopRouteBlackhole.json", id = STATIC_ROUTE_PATH) StaticRoutes route)
            throws WriteFailedException {
        final Route route1 = getIpv4RouteWithId(route, 1L);
        noMappingDefined(mappingContext, namesFactory.uniqueRouteName(ROUTE_PROTOCOL_NAME, route1), "route-context");

        customizer.writeCurrentAttributes(validId, route1, writeContext);
        verifyInvocation(1, ImmutableList
                        .of(desiredSpecialResult(1, 0, FIRST_ADDRESS_AS_ARRAY, 24, 1, 0, 0, 0, ROUTE_PROTOCOL_INDEX, 0)), api,
                requestCaptor);
    }

    @Test
    public void testUpdate(
            @InjectTestData(resourcePath = "/ipv4/specialhop/specialHopRouteBlackhole.json", id = STATIC_ROUTE_PATH) StaticRoutes route) {
        try {
            customizer.updateCurrentAttributes(validId, new RouteBuilder().build(), getIpv4RouteWithId(route, 1L),
                    writeContext);
        } catch (WriteFailedException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
            verifyNotInvoked(api);
            return;
        }
        fail("Test should have thrown exception");
    }


    @Test
    public void testDeleteSingleHop(
            @InjectTestData(resourcePath = "/ipv4/simplehop/simpleHopRouteWithClassifier.json", id = STATIC_ROUTE_PATH) StaticRoutes route)
            throws WriteFailedException {
        customizer.deleteCurrentAttributes(validId, getIpv4RouteWithId(route, 1L), writeContext);
        verifyInvocation(1, ImmutableList
                .of(desiredFlaglessResult(0, 0, 0, FIRST_ADDRESS_AS_ARRAY, 24,
                        SECOND_ADDRESS_AS_ARRAY, INTERFACE_INDEX,
                        0, ROUTE_PROTOCOL_INDEX, 1, 0, CLASSIFY_TABLE_INDEX, 1)), api, requestCaptor);
    }

    @Test
    public void testDeleteHopList(
            @InjectTestData(resourcePath = "/ipv4/multihop/multiHopRouteWithClassifier.json", id = STATIC_ROUTE_PATH) StaticRoutes route)
            throws WriteFailedException {
        final Route route1 = getIpv4RouteWithId(route, 1L);
        noMappingDefined(mappingContext, namesFactory.uniqueRouteName(ROUTE_PROTOCOL_NAME, route1), "route-context");

        customizer.deleteCurrentAttributes(validId, route1, writeContext);
        verifyInvocation(2,
                ImmutableList.of(
                        desiredFlaglessResult(0, 0, 1, FIRST_ADDRESS_AS_ARRAY, 24,
                                FIRST_ADDRESS_AS_ARRAY, INTERFACE_INDEX, 2, ROUTE_PROTOCOL_INDEX, 1, 0,
                                CLASSIFY_TABLE_INDEX, 1),
                        desiredFlaglessResult(0, 0, 1, FIRST_ADDRESS_AS_ARRAY, 24,
                                new byte[]{-64, -88, 2, 2}, INTERFACE_INDEX, 3, ROUTE_PROTOCOL_INDEX, 1, 0,
                                CLASSIFY_TABLE_INDEX, 1)), api, requestCaptor);

        verify(routeHopContext, times(1))
                .removeChild(
                        namesFactory.uniqueRouteName(ROUTE_PROTOCOL_NAME, route1),
                        namesFactory.uniqueRouteHopName(getHopWithId(route1, 1)),
                        mappingContext);
        verify(routeHopContext, times(1))
                .removeChild(
                        namesFactory.uniqueRouteName(ROUTE_PROTOCOL_NAME, route1),
                        namesFactory.uniqueRouteHopName(getHopWithId(route1, 2)),
                        mappingContext);
    }

    @Test
    public void testDeleteSpecialHop(
            @InjectTestData(resourcePath = "/ipv4/specialhop/specialHopRouteBlackhole.json", id = STATIC_ROUTE_PATH) StaticRoutes route)
            throws WriteFailedException {
        customizer.deleteCurrentAttributes(validId, getIpv4RouteWithId(route, 1L), writeContext);

        verifyInvocation(1,
                ImmutableList.of(desiredSpecialResult(0, 0, FIRST_ADDRESS_AS_ARRAY, 24, 1, 0, 0, 0, ROUTE_PROTOCOL_INDEX, 0)), api,
                requestCaptor);
    }
}
