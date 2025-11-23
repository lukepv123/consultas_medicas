package curso.petenusso.clinicsapp.data.medico

import curso.petenusso.clinicsapp.api.dto.AccountDTO
import curso.petenusso.clinicsapp.api.medico.MedicoApi
import curso.petenusso.clinicsapp.api.medico.dto.*
import curso.petenusso.clinicsapp.core.AppResult
import curso.petenusso.clinicsapp.core.runCatchingResult

class MedicoRepository {

    private val api = MedicoApi.create()

    suspend fun criarComAccount(
        crm: String,
        nome: String,
        especialidadeEnumName: String,
        email: String,
        senha: String
    ): AppResult<CreateMedicoResponse> = runCatchingResult {
        api.criar(
            CreateMedicoRequest(
                crm = crm,
                nome = nome,
                especialidade = especialidadeEnumName,
                account = AccountDTO(email = email, senha = senha)
            )
        )
    }


    suspend fun listar() = runCatchingResult { api.listar() }



//
//
//    suspend fun atualizar(id: String, dto: MedicoDTO) =
//        runCatchingResult { api.atualizar(id, dto) }
//
//    suspend fun buscar(id: String) = runCatchingResult { api.buscar(id) }
//
//    suspend fun remover(id: String) = runCatchingResult { api.remover(id) }
}
