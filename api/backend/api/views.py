from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.parsers import JSONParser
from django.contrib.auth.models import User
from .serializers import UsuarioSerializer
import json
from django.contrib.auth import authenticate, login
from .models import AlimentoPlanoAlimentar
from .serializers import RefeicaoSerializer

@csrf_exempt
def cadastrar_usuario(request):

    if request.method == "POST":
        try:

            data = JSONParser().parse(request)

            if 'userName' in data:
                data['username'] = data.pop('userName')

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
            
            if 'username' not in data or 'password' not in data:
                return JsonResponse({
                    "error": "Username e senha são obrigatórios"
                }, status=400)
            
            username = data['username']
            password = data['password']

            user = authenticate(request, username=username, password=password)
            
            if user is not None:
                login(request, user)
                
                return JsonResponse({
                    "message": "Login bem-sucedido!",
                    "usuario": {
                        "id": user.id,
                        "username": user.username,
                        "email": user.email
                    }
                }, status=200)
            else:
              
                return JsonResponse({
                    "error": "Credenciais inválidas"
                }, status=401)
                
        except json.JSONDecodeError:
            return JsonResponse({"error": "JSON inválido"}, status=400)
        except Exception as e:

            return JsonResponse({"error": "Erro interno do servidor", "detalhes": str(e)}, status=500)
    
    return JsonResponse({"error": "Método não permitido"}, status=405)


def salvar_refeicao(request):
    if request.method == "POST":
        try:
            data = JSONParser().parse(request)
            
            if 'usuario_id' in data:
                data['usuario'] = data.pop('usuario_id')

            serializer = RefeicaoSerializer(data=data)
            
            if serializer.is_valid():
                serializer.save()
                return JsonResponse({
                    "message": "Refeição salva com sucesso!",
                    "id": serializer.data['id']
                }, status=201)
            else:
                return JsonResponse({
                    "error": "Dados inválidos", 
                    "detalhes": serializer.errors
                }, status=400)
                
        except Exception as e:
            return JsonResponse({
                "error": "Erro interno do servidor", 
                "detalhes": str(e)
            }, status=500)
    
    return JsonResponse({"error": "Método não permitido"}, status=405)


def listar_refeicoes(request):
    if request.method == "GET":
        usuario_id = request.GET.get('usuario_id')
        
        if not usuario_id:
            return JsonResponse({"error": "usuario_id é obrigatório"}, status=400)

        refeicoes = AlimentoPlanoAlimentar.objects.filter(usuario_id=usuario_id)
        
        serializer = RefeicaoSerializer(refeicoes, many=True)
        return JsonResponse(serializer.data, safe=False, status=200)
    
    return JsonResponse({"error": "Método não permitido"}, status=405)


@csrf_exempt
def refeicoes_dispatch(request):
    if request.method == 'POST':
        return salvar_refeicao(request)
    elif request.method == 'GET':
        return listar_refeicoes(request)
    return JsonResponse({"error": "Método não permitido"}, status=405)

@csrf_exempt
def atualizar_status_refeicao(request, refeicao_id):
    if request.method == "PATCH": # PATCH é usado para atualizações parciais
        try:
            # Busca a refeição pelo ID
            try:
                refeicao = AlimentoPlanoAlimentar.objects.get(pk=refeicao_id)
            except AlimentoPlanoAlimentar.DoesNotExist:
                return JsonResponse({"error": "Refeição não encontrada"}, status=404)

            # Lê o novo status do corpo da requisição
            data = json.loads(request.body.decode('utf-8'))
            
            if 'concluido' in data:
                refeicao.concluido = data['concluido']
                refeicao.save()
                return JsonResponse({"message": "Status atualizado com sucesso!"}, status=200)
            else:
                return JsonResponse({"error": "Campo 'concluido' é obrigatório"}, status=400)

        except Exception as e:
            return JsonResponse({"error": "Erro interno", "detalhes": str(e)}, status=500)

    return JsonResponse({"error": "Método não permitido"}, status=405)