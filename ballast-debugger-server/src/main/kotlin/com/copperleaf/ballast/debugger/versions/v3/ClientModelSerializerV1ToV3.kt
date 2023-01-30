package com.copperleaf.ballast.debugger.versions.v3

import com.copperleaf.ballast.debugger.versions.CompositeModelConverter
import com.copperleaf.ballast.debugger.versions.CompositeModelSerializer
import com.copperleaf.ballast.debugger.versions.v1.BallastDebuggerActionV1
import com.copperleaf.ballast.debugger.versions.v1.BallastDebuggerEventV1
import com.copperleaf.ballast.debugger.versions.v1.ClientModelSerializerV1
import com.copperleaf.ballast.debugger.versions.v2.ClientModelConverterV1ToV2

public class ClientModelSerializerV1ToV3() : CompositeModelSerializer<
        BallastDebuggerEventV1,
        BallastDebuggerEventV3,
        BallastDebuggerActionV1,
        BallastDebuggerActionV3>(
    serializer = ClientModelSerializerV1(),
    converter = CompositeModelConverter(
        ClientModelConverterV1ToV2(),
        ClientModelConverterV2ToV3(),
    ),
)
