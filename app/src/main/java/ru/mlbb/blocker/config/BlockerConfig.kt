package ru.mlbb.blocker.config

import android.content.Context
import ru.mlbb.blocker.data.BlockerPolicy
import ru.mlbb.blocker.data.BlocklistStore
import ru.mlbb.blocker.data.MatchPhase
import ru.mlbb.blocker.data.MatchPhaseController
import ru.mlbb.blocker.data.PhaseFilter
import ru.mlbb.blocker.data.RegionIpRepository
import ru.mlbb.blocker.data.RegionProfiles
import ru.mlbb.blocker.data.StartupEndpoints
import java.net.InetAddress
import java.net.ServerSocket
import java.security.SecureRandom

object BlockerConfig {

    @Volatile
    private var currentPolicy: BlockerPolicy = BlockerPolicy()

    /** Фазово-зависимая политика, которую использует монитор для классификации блокировок. */
    fun currentPolicy(): BlockerPolicy = currentPolicy

    fun build(
        context: Context,
        port: Int,
        secret: String,
        phase: MatchPhase = MatchPhase.IDLE,
        delayRelayPort: Int = 0,
    ): String {
        val store = BlocklistStore(context)
        val profile = store.selectedProfile?.let(RegionProfiles::byId)
        val profileRules = profile?.rules?.map { it.first }.orEmpty()
        val rules = store.activeValues().distinct()
        val allowRules = (
            store.activeAllowValues() +
                profile?.allowedCidrs.orEmpty() +
                if (store.allowStartupServer) StartupEndpoints.CIDRS else emptyList()
            ).distinct()
        val loginRules = if (store.allowStartupServer) {
            StartupEndpoints.LOGIN_POOL_CIDRS
        } else {
            emptyList()
        }
        val loginPorts = if (loginRules.isEmpty()) emptyList() else listOf(StartupEndpoints.LOGIN_PORT)

        val filter = MatchPhaseController.filterFor(phase)
        val blocksEnabled = filter == PhaseFilter.ACTIVE
        val delayActive = filter != PhaseFilter.BLOCKS_OFF &&
            store.filterMode == "delay" &&
            delayRelayPort > 0
        val delayCidrs = if (delayActive) RegionIpRepository.delayTargetCidrs() else emptyList()

        currentPolicy = BlockerPolicy(
            allowCidrs = allowRules,
            startupCidrs = StartupEndpoints.CIDRS,
            userBlockCidrs = if (blocksEnabled) rules else emptyList(),
            profileBlockCidrs = if (blocksEnabled) profileRules else emptyList(),
            blockOtherMatchPorts = blocksEnabled && (profile?.blockOtherMatchServers == true),
            loginCidrs = loginRules,
            loginPorts = loginPorts.toSet(),
        )

        return SingBoxConfigBuilder.build(
            rules = rules,
            profileRules = profileRules,
            allowRules = allowRules,
            loginRules = loginRules,
            loginPorts = loginPorts,
            blockMatchPorts = profile?.blockOtherMatchServers == true,
            discoveryMode = store.discoveryMode || System.currentTimeMillis() < store.discoveryUntil,
            clashPort = port,
            clashSecret = secret,
            rejectMethod = if (store.fastReject) {
                SingBoxConfigBuilder.REJECT_RESET
            } else {
                SingBoxConfigBuilder.REJECT_DROP
            },
            logOutput = java.io.File(context.filesDir, "singbox.log").absolutePath,
            logLevel = if (store.logBlockedAttempts) "debug" else "info",
            blocksEnabled = blocksEnabled,
            delayEnabled = filter != PhaseFilter.BLOCKS_OFF,
            delayCidrs = delayCidrs,
            delayRelayPort = delayRelayPort,
        )
    }

    fun newPort(): Int {
        return try {
            ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { it.localPort }
        } catch (_: Exception) {
            39090
        }
    }

    fun newSecret(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
