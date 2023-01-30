package com.copperleaf.ballast.debugger.versions.v2

import com.copperleaf.ballast.debugger.versions.CompositeModelSerializer
import com.copperleaf.ballast.debugger.versions.v1.BallastDebuggerActionV1
import com.copperleaf.ballast.debugger.versions.v1.BallastDebuggerEventV1
import com.copperleaf.ballast.debugger.versions.v1.ClientModelSerializerV1

public class ClientModelSerializerV1ToV2 : CompositeModelSerializer<
        BallastDebuggerEventV1,
        BallastDebuggerEventV2,
        BallastDebuggerActionV1,
        BallastDebuggerActionV2>(
    serializer = ClientModelSerializerV1(),
    converter = ClientModelConverterV1ToV2(),
)
