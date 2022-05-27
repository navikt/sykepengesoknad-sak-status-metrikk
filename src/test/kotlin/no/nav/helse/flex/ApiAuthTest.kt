package no.nav.helse.flex

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

class ApiAuthTest : FellesTestOppsett() {

    val fnr = "12343787332"

    @Test
    fun `krever riktig audience`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/meldinger")
                .header("Authorization", "Bearer ${tokenxToken(fnr = fnr, audience = "facebook")}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `krever riktig client id`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/meldinger")
                .header("Authorization", "Bearer ${tokenxToken(fnr = fnr, clientId = "facebook")}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `krever riktig idp`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/meldinger")
                .header("Authorization", "Bearer ${tokenxToken(fnr = fnr, issuerId = "loginservice")}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }
}
