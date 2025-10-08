from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.parsers import JSONParser
from django.contrib.auth.models import User
from .serializers import UsuarioSerializer
import json
from django.contrib.auth import authenticate, login

@csrf_exempt
def cadastrar_usuario(request):

    if request.method == "POST":
        try:
            # Verifica o body raw
            raw_body = request.body.decode('utf-8')
            data = JSONParser().parse(request)
            
           # Note a mudança: 'userName' deve ser enviado como 'username' no JSON
            if 'userName' in data:
                data['username'] = data.pop('userName')

            # Verifica se todos os campos necessários estão presentes
            campos_obrigatorios = ['username', 'name', 'email', 'password']
            for campo in campos_obrigatorios:
                if campo not in data:
                    return JsonResponse({"error": f"Campo faltando: {campo}"}, status=400)

            serializer = UsuarioSerializer(data=data)
            if not serializer.is_valid():
                return JsonResponse({
                    "error": "Dados inválidos",
                    "detalhes": serializer.errors
                }, status=400)
            
            usuario = serializer.save()
            
            return JsonResponse({
                "message": "Usuário cadastrado com sucesso!",
                "id": usuario.id,
                "username": usuario.username
            }, status=201)
            
        except Exception as e:
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
            print("🔐 Dados de login recebidos:", data['username'])
            
            # ✅ Valida os campos obrigatórios, esperando 'username' e 'password'
            if 'username' not in data or 'password' not in data:
                return JsonResponse({
                    "error": "Username e senha são obrigatórios"
                }, status=400)
            
            username = data['username']
            password = data['password']
            
            # ✅ A função authenticate é a maneira correta de verificar as credenciais
            user = authenticate(request, username=username, password=password)
            
            if user is not None:
                # ✅ Login bem-sucedido
                # A função login(request, user) cria uma sessão, o que pode ser útil
                # para o backend, mesmo em APIs REST.
                login(request, user)
                print(f"✅ Login bem-sucedido para: {user.username}")
                return JsonResponse({
                    "message": "Login bem-sucedido!",
                    "usuario": {
                        "id": user.id,
                        "username": user.username,
                        "email": user.email
                    }
                }, status=200)
            else:
                # ✅ Falha na autenticação
                print(f"❌ Falha de login para: {username}")
                return JsonResponse({
                    "error": "Credenciais inválidas"
                }, status=401)
                
        except json.JSONDecodeError:
            return JsonResponse({"error": "JSON inválido"}, status=400)
        except Exception as e:
            print("💥 Erro no login:", str(e))
            return JsonResponse({"error": "Erro interno do servidor", "detalhes": str(e)}, status=500)
    
    return JsonResponse({"error": "Método não permitido"}, status=405)