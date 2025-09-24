from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.parsers import JSONParser
from .models import Usuario
from .serializers import UsuarioSerializer
import json
from django.contrib.auth.hashers import check_password


@csrf_exempt
def cadastrar_usuario(request):

    if request.method == "POST":
        try:
            # Debug completo
            print("=" * 50)
            print("📱 REQUISIÇÃO RECEBIDA DO ANDROID")
            print("=" * 50)
            
            # Verifica o body raw
            raw_body = request.body.decode('utf-8')
            print("📦 Raw body:", raw_body)
            
            # Tenta fazer parse com JSONParser
            data = JSONParser().parse(request)
            print("📊 Data parsed:", data)
            print("📊 Tipo dos dados:", type(data))
            
            # Verifica se todos os campos necessários estão presentes
            campos_obrigatorios = ['userName', 'name', 'email', 'password']
            for campo in campos_obrigatorios:
                if campo not in data:
                    print(f"❌ Campo faltando: {campo}")
                else:
                    print(f"✅ Campo {campo}: {data[campo]}")
            
            # Testa o serializer
            serializer = UsuarioSerializer(data=data)
            print("🔍 Serializer é válido?", serializer.is_valid())
            
            if not serializer.is_valid():
                print("❌ Erros de validação:", serializer.errors)
                return JsonResponse({
                    "error": "Dados inválidos",
                    "detalhes": serializer.errors
                }, status=400)
            
            # Salva o usuário
            usuario = serializer.save()
            print(f"✅ Usuário salvo com ID: {usuario.id}")
            
            return JsonResponse({
                "message": "Usuário cadastrado com sucesso!",
                "id": usuario.id,
                "userName": usuario.userName
            }, status=201)
            
        except Exception as e:
            print("💥 EXCEÇÃO:", str(e))
            import traceback
            traceback.print_exc()
            
            return JsonResponse({
                "error": "Erro interno do servidor",
                "detalhes": str(e)
            }, status=500)
    else:
        return JsonResponse({"error": "Método não permitido"}, status=405)
    
@csrf_exempt
def login_usuario(request):
    if request.method == "POST":
        try:
            data = json.loads(request.body.decode('utf-8'))
            print("🔐 Dados de login recebidos:", data)
            
            # Valida campos obrigatórios
            if 'userName' not in data or 'password' not in data:
                return JsonResponse({
                    "error": "Username e senha são obrigatórios"
                }, status=400)
            
            username = data['userName']
            password = data['password']
            
            # Busca o usuário pelo username
            try:
                usuario = Usuario.objects.get(userName=username)
            except Usuario.DoesNotExist:
                return JsonResponse({
                    "error": "Usuário não encontrado"
                }, status=404)
            
            # Verifica a senha
            if check_password(password, usuario.password):
                print(f"✅ Login bem-sucedido para: {usuario.userName}")
                return JsonResponse({
                    "message": "Login bem-sucedido!",
                    "usuario": {
                        "id": usuario.id,
                        "userName": usuario.userName,
                        "name": usuario.name,
                        "email": usuario.email
                    }
                }, status=200)
            else:
                return JsonResponse({
                    "error": "Senha incorreta"
                }, status=401)
                
        except json.JSONDecodeError:
            return JsonResponse({"error": "JSON inválido"}, status=400)
        except Exception as e:
            print("💥 Erro no login:", str(e))
            return JsonResponse({"error": "Erro interno do servidor"}, status=500)
    
    return JsonResponse({"error": "Método não permitido"}, status=405)