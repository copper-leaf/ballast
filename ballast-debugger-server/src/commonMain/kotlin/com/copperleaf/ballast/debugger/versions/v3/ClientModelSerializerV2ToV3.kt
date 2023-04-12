package com.copperleaf.ballast.debugger.versions.v3

import com.copperleaf.ballast.debugger.versions.CompositeModelSerializer
import com.copperleaf.ballast.debugger.versions.v2.BallastDebuggerActionV2
import com.copperleaf.ballast.debugger.versions.v2.BallastDebuggerEventV2
import com.copperleaf.ballast.debugger.versions.v2.ClientModelSerializerV2

public class ClientModelSerializerV2ToV3() : CompositeModelSerializer<
        BallastDebuggerEventV2,
        BallastDebuggerEventV3,
        BallastDebuggerActionV2,
        BallastDebuggerActionV3>(
        serializer = ClientModelSerializerV2(),
        converter = ClientModelConverterV2ToV3(),
    )
